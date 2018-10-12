package codechicken.multipart.handler

import java.io.{ByteArrayOutputStream, DataOutputStream}
import java.util.{Iterator => JIterator, LinkedList => JLinkedList, Map => JMap}

import codechicken.lib.data.MCDataOutputWrapper
import codechicken.lib.packet.ICustomPacketHandler.{IClientPacketHandler, IServerPacketHandler}
import codechicken.lib.packet.{IHandshakeHandler, PacketCustom}
import codechicken.multipart.handler.MultipartProxy._
import codechicken.multipart._
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.network.NetHandlerPlayServer
import net.minecraft.network.play.server.SPacketDisconnect
import net.minecraft.network.play.{INetHandlerPlayClient, INetHandlerPlayServer}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumHand
import net.minecraft.util.math.{BlockPos, ChunkPos}
import net.minecraft.util.text.{TextComponentString, TextComponentTranslation}
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk

import scala.collection.JavaConversions._
import scala.collection.mutable.{HashMap => MHashMap, Map => MMap, MultiMap => MMultiMap, Set => MSet}

class MultipartPH {
    val channel = MultipartMod
    val registryChannel = "ForgeMultipart"
}

object MultipartCPH extends MultipartPH with IClientPacketHandler {
    def handlePacket(packet: PacketCustom, mc: Minecraft, netHandler: INetHandlerPlayClient) {
        try {
            packet.getType match {
                case 1 => handlePartRegistration(packet, netHandler)
                case 2 => handleCompressedTileDesc(packet, mc.world)
                case 3 => handleCompressedTileData(packet, mc.world)
            }
        }
        catch {
            case e: RuntimeException if e.getMessage != null && e.getMessage.startsWith("DC: ") =>
                netHandler.handleDisconnect(new SPacketDisconnect(new TextComponentString(e.getMessage.substring(4))))
        }
    }

    def handlePartRegistration(packet: PacketCustom, netHandler: INetHandlerPlayClient) {
        val missing = MultiPartRegistry.readIDMap(packet)
        if (missing.nonEmpty) {
            netHandler.handleDisconnect(new SPacketDisconnect(new TextComponentTranslation("multipart.missing", missing.mkString(", "))))
        }
    }

    def handleCompressedTileDesc(packet: PacketCustom, world: World) {
        val cc = new ChunkPos(packet.readInt, packet.readInt)
        val num = packet.readUShort
        for (i <- 0 until num)
            TileMultipart.handleDescPacket(world, indexInChunk(cc, packet.readShort), packet)
    }

    def handleCompressedTileData(packet: PacketCustom, world: World) {
        var x = packet.readInt
        while (x != Int.MaxValue) {
            val pos = new BlockPos(x, packet.readInt, packet.readInt)
            var i = packet.readUByte
            while (i < 255) {
                TileMultipart.handlePacket(pos, world, i, packet)
                i = packet.readUByte
            }
            x = packet.readInt
        }
    }
}

object MultipartSPH extends MultipartPH with IServerPacketHandler with IHandshakeHandler {

    @Deprecated
    //Now exists in CCL.
    class MCByteStream(bout: ByteArrayOutputStream) extends codechicken.lib.data.MCByteStream(bout) {
        override def getBytes = bout.toByteArray
    }

    private val updateMap = MMap[World, MMap[BlockPos, MCByteStream]]()
    /**
     * These maps are keyed by entityID so that new player instances with the same entity id don't conflict world references
     */
    private val chunkWatchers = new MHashMap[Int, MSet[ChunkPos]] with MMultiMap[Int, ChunkPos]
    private val newWatchers = MMap[Int, JLinkedList[ChunkPos]]()

    def handlePacket(packet: PacketCustom, sender: EntityPlayerMP, netHandler: INetHandlerPlayServer) {
        packet.getType match {
            case 1 => ControlKeyModifer.map.put(sender, packet.readBoolean)
            case 10 => ItemPlacementHelper.place(sender, if(packet.readBoolean()) EnumHand.MAIN_HAND else EnumHand.OFF_HAND, sender.world)
        }
    }

    def handshakeReceived(netHandler: NetHandlerPlayServer) {
        val packet = new PacketCustom(registryChannel, 1)
        MultiPartRegistry.writeIDMap(packet)
        netHandler.sendPacket(packet.toPacket)
    }

    def onWorldUnload(world: World) {
        if (!world.isRemote) {
            updateMap.remove(world)
        }
    }

    def getTileStream(world: World, pos: BlockPos) =
        updateMap.getOrElseUpdate(world, {
            if (world.isRemote) {
                throw new IllegalArgumentException("Cannot use MultipartSPH on a client world")
            }
            MMap()
        }).getOrElseUpdate(pos, {
            val s = new MCByteStream(new ByteArrayOutputStream)
            s.writePos(pos)
            s
        })

    def onTickEnd(players: Seq[EntityPlayerMP]) {
        PacketScheduler.sendScheduled()

        for (p <- players if chunkWatchers.containsKey(p.getEntityId)) {
            updateMap.get(p.world) match {
                case Some(m) if m.nonEmpty =>
                    val chunks = chunkWatchers(p.getEntityId)
                    val packet = new PacketCustom(channel, 3).compress()

                    var send = false
                    for ((pos, stream) <- m if chunks(new ChunkPos(pos.getX >> 4, pos.getZ >> 4))) {
                        send = true
                        packet.writeArray(stream.getBytes)
                        packet.writeByte(255) //terminator
                    }
                    if (send) {
                        packet.writeInt(Int.MaxValue) //terminator
                        packet.sendToPlayer(p)
                    }
                case _ =>
            }
        }
        updateMap.foreach(_._2.clear())
        for (p <- players if newWatchers.containsKey(p.getEntityId)) {
            for (c <- newWatchers(p.getEntityId)) {
                val chunk = p.world.getChunkFromChunkCoords(c.x, c.z)
                val pkt = getDescPacket(chunk, chunk.getTileEntityMap.values.iterator)
                if (pkt != null) pkt.sendToPlayer(p)
                chunkWatchers.addBinding(p.getEntityId, c)
            }
        }
        newWatchers.clear()
    }

    def onChunkWatch(p: EntityPlayer, c: ChunkPos) {
        newWatchers.getOrElseUpdate(p.getEntityId, new JLinkedList).add(c)
    }

    def onChunkUnWatch(p: EntityPlayer, c: ChunkPos) {
        newWatchers.get(p.getEntityId) match {
            case Some(chunks) => chunks.remove(c)
            case _ =>
        }
        chunkWatchers.removeBinding(p.getEntityId, c)
    }

    def getDescPacket(chunk: Chunk, it: JIterator[TileEntity]): PacketCustom = {
        val s = new MCByteStream(new ByteArrayOutputStream)

        var num = 0
        while (it.hasNext) {
            val tile = it.next
            if (tile.isInstanceOf[TileMultipart]) {
                s.writeShort(indexInChunk(tile.getPos))
                tile.asInstanceOf[TileMultipart].writeDesc(s)
                num += 1
            }
        }
        if (num != 0) {
            val packet = new PacketCustom(channel, 2).compress()
            packet.writeInt(chunk.x).writeInt(chunk.z)
                .writeShort(num)
                .writeArray(s.getBytes)
            return packet
        }
        null
    }
}
