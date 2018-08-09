package codechicken.multipart

import java.util.{BitSet => JBitSet}

import codechicken.lib.packet.PacketCustom
import codechicken.multipart.asm.ASMImplicits._
import codechicken.multipart.asm.{MultipartMixinFactory, ScratchBitSet}
import codechicken.multipart.handler.MultipartProxy
import net.minecraft.network.play.server.SPacketBlockChange
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

import scala.collection.mutable.{Map => MMap}

/**
 * This class manages the dynamic construction and allocation of container TileMultipart instances.
 *
 * Classes that extend TileMultipart, adding tile centric logic, optimisations or interfaces, can be registered to a marker interface on a part instance.
 * When a part is added to the tile that implements the certain marker interface, the container tile will be replaced with a class that includes the functionality from the corresponding mixin class.
 *
 * Classes are generated in a similar fashion to the way scala traits are compiled. To see the output, simply enable the config option and look in the asm/multipart folder of you .minecraft directory.
 *
 * There are several mixin traits that come with the API included in the scalatraits package. TPartialOcclusionTile is defined as class instead of trait to give an example for Java programmers.
 */
object MultipartGenerator extends ScratchBitSet {
    private val tileTraitMap = MMap[Class[_], JBitSet]()
    private val interfaceTraitMap_c = MMap[String, String]()
    private val interfaceTraitMap_s = MMap[String, String]()
    private val partTraitMap_c = MMap[Class[_], JBitSet]()
    private val partTraitMap_s = MMap[Class[_], JBitSet]()
    private val clientTraitId = MultipartMixinFactory.registerTrait(classOf[TileMultipartClient])

    private def partTraitMap(client: Boolean) = if (client) partTraitMap_c else partTraitMap_s

    private def interfaceTraitMap(client: Boolean) = if (client) interfaceTraitMap_c else interfaceTraitMap_s

    private def traitsForPart(part: TMultiPart, client: Boolean) =
        partTraitMap(client).getOrElseUpdate(part.getClass, {
            def heirachy(clazz: Class[_]): Seq[Class[_]] = {
                var superClasses: Seq[Class[_]] = clazz.getInterfaces.flatMap(c => heirachy(c)) :+ clazz
                if (clazz.getSuperclass != null) {
                    superClasses = superClasses ++ heirachy(clazz.getSuperclass)
                }
                superClasses
            }

            val map = interfaceTraitMap(client)
            val traits = heirachy(part.getClass).flatMap(c => map.get(c.nodeName)).distinct

            val bitset = new JBitSet
            traits.map(MultipartMixinFactory.getId).foreach(bitset.set)
            bitset
        })

    private def setTraits(part: TMultiPart, client: Boolean): JBitSet = setTraits(Seq(part), client)

    private def setTraits(parts: Iterable[TMultiPart], client: Boolean) = {
        val bitset = freshBitSet
        parts.foreach(p => bitset.or(traitsForPart(p, client)))
        if (client) bitset.set(clientTraitId)
        bitset
    }

    /**
     * Check if part adds any new interfaces to tile, if so, replace tile with a new copy and call tile.addPart(part)
     * returns true if tile was replaced
     */
    private[multipart] def addPart(world: World, pos: BlockPos, part: TMultiPart): TileMultipart = {
        val (tile, converted) = TileMultipart.getOrConvertTile2(world, pos)
        val bitset = setTraits(part, world.isRemote)

        var ntile = tile
        if (ntile != null) {
            if (converted) {
                //perform client conversion
                ntile.partList(0).invalidateConvertedTile()
                world.setBlockState(pos, BlockMultipart.getRuntimeState, 0)
                silentAddTile(world, pos, ntile)
                PacketCustom.sendToChunk(new SPacketBlockChange(world, pos), world, pos.getX >> 4, pos.getZ >> 4)
                ntile.partList(0).onConverted()
                ntile.writeAddPart(ntile.partList(0))
            }

            val tileTraits = tileTraitMap(tile.getClass)
            bitset.andNot(tileTraits)
            if (!bitset.isEmpty) {
                bitset.or(tileTraits)
                ntile = MultipartMixinFactory.construct(bitset)
                tile.setValid(false)
                silentAddTile(world, pos, ntile)
                ntile.from(tile)
            }
        }
        else {
            world.setBlockState(pos, BlockMultipart.getRuntimeState, 0)
            ntile = MultipartMixinFactory.construct(bitset)
            silentAddTile(world, pos, ntile)
        }
        ntile.addPart_impl(part)
        ntile
    }

    /**
     * Adds a tile entity to the world without notifying neighbor blocks or adding it to the tick list
     */
    def silentAddTile(world: World, pos: BlockPos, tile: TileEntity) {
        val chunk = world.getChunkFromBlockCoords(pos)
        if (chunk != null) {
            chunk.addTileEntity(pos, tile)
        }
    }

    /**
     * Check if tile satisfies all the interfaces required by parts. If not, return a new generated copy of tile
     */
    private[multipart] def generateCompositeTile(tile: TileEntity, parts: Iterable[TMultiPart], client: Boolean) = {
        val bitset = setTraits(parts, client)
        if (tile != null && tile.isInstanceOf[TileMultipart] && bitset == tileTraitMap(tile.getClass)) {
            tile.asInstanceOf[TileMultipart]
        } else {
            MultipartMixinFactory.construct(bitset)
        }
    }

    /**
     * Check if there are any redundant interfaces on tile, if so, replace tile with new copy
     */
    private[multipart] def partRemoved(tile: TileMultipart) = {
        val ntile = generateCompositeTile(tile, tile.partList, tile.getWorld.isRemote)
        if (ntile != tile) {
            tile.setValid(false)
            silentAddTile(tile.getWorld, tile.getPos, ntile)
            ntile.from(tile)
            ntile.notifyTileChange()
        }
        ntile
    }

    /**
     * register s_trait to be applied to tiles containing parts implementing s_interface
     */
    def registerTrait(s_interface: String, s_trait: String): Unit = registerTrait(s_interface, s_trait, s_trait)

    /**
     * register traits to be applied to tiles containing parts implementing s_interface
     * c_trait for client worlds (may be null)
     * s_trait for server worlds (may be null)
     */
    def registerTrait(s_interface$: String, c_trait$: String, s_trait$: String) {
        val s_interface = nodeName(s_interface$)
        val c_trait = nodeName(c_trait$)
        val s_trait = nodeName(s_trait$)

        def registerSide(map: MMap[String, String], traitName: String) = if (traitName != null) {
            if (map.contains(s_interface)) {
                logger.error("Trait already registered for " + s_interface)
            } else {
                map.put(s_interface, traitName)
                MultipartMixinFactory.registerTrait(traitName)
            }
        }

        registerSide(interfaceTraitMap_c, c_trait)
        registerSide(interfaceTraitMap_s, s_trait)
    }

    def registerPassThroughInterface(s_interface: String): Unit = registerPassThroughInterface(s_interface, true, true)

    /**
     * A passthrough interface, is an interface to be implemented on the container tile instance, for which all calls are passed directly to the single implementing part.
     * Registering a passthrough interface is equivalent to defining a mixin class as follows.
     *  1. field 'impl' which contains the reference to the corresponding part
     *  2. occlusionTest is overriden to prevent more than one part with s_interface existing in the block space
     *  3. implementing s_interface and passing all calls directly to the part instance.
     *
     * This allows compatibility with APIs that expect interfaces on the tile entity.
     */
    def registerPassThroughInterface(s_interface: String, client: Boolean, server: Boolean) {
        val tType = MultipartMixinFactory.generatePassThroughTrait(s_interface)
        if (tType == null) {
            return
        }

        registerTrait(s_interface, if (client) tType else null, if (server) tType else null)
    }

    private[multipart] def registerTileClass(clazz: Class[_ <: TileMultipart], traits: JBitSet) {
        tileTraitMap.put(clazz, traits.copy)
        MultipartProxy.onTileClassBuilt(clazz)
    }
}
