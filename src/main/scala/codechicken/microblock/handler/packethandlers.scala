package codechicken.microblock.handler

import codechicken.lib.packet.ICustomPacketHandler.{IClientPacketHandler, IServerPacketHandler}
import codechicken.lib.packet.{IHandshakeHandler, PacketCustom}
import codechicken.microblock.MicroMaterialRegistry
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.NetHandlerPlayServer
import net.minecraft.network.play.server.SPacketDisconnect
import net.minecraft.network.play.{INetHandlerPlayClient, INetHandlerPlayServer}
import net.minecraft.util.text.TextComponentString

class MicroblockPH
{
    val registryChannel = "ForgeMicroblock" //Must use the 250 system for ID registry as the NetworkMod idMap hasn't been properly initialized from the server yet.
}

object MicroblockCPH extends MicroblockPH with IClientPacketHandler
{
    def handlePacket(packet: PacketCustom, mc: Minecraft, netHandler:INetHandlerPlayClient) {
        packet.getType match {
            case 1 => handleMaterialRegistration(packet, netHandler)
        }
    }

    def handleMaterialRegistration(packet: PacketCustom, netHandler: INetHandlerPlayClient) {
        val missing = MicroMaterialRegistry.readIDMap(packet)
        if (!missing.isEmpty)
            netHandler.handleDisconnect(new SPacketDisconnect(new TextComponentString("microblock.missing"+missing.mkString(", "))))
    }
}

object MicroblockSPH extends MicroblockPH with IServerPacketHandler with IHandshakeHandler
{
    def handlePacket(packet: PacketCustom, sender: EntityPlayerMP, netHandler:INetHandlerPlayServer) {}


    override def handshakeReceived(netHandler:NetHandlerPlayServer)
    {
        val packet = new PacketCustom(registryChannel, 1)
        MicroMaterialRegistry.writeIDMap(packet)
        netHandler.sendPacket(packet.toPacket)
    }
}
