package codechicken.multipart.handler

import java.util.Collections

import codechicken.multipart
import codechicken.multipart.{TileMultipart, WrappedTileEntityRegistry}
import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{ITickable, ResourceLocation}
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.fml.common.registry.GameRegistry

/**
 * Contains the holder tile that is loaded from NBT.
 * The holder tile saves the nbt from readFromNBT and uses it in onLoad to replace the tile in world with the ASM generated composite tile.
 */
object MultipartSaveLoad {

    val TILE_ID: ResourceLocation = new ResourceLocation("ccmp:saved_multipart")

    /**
     * Holder tile for MultiParts that haven't yet been fully loaded.
     * This is a tickable tile, as a result we have basic support for things that don't
     * know how a MultiPart should be loaded, this has some incredibly minor performance
     * impacts, tiles will also be notified of the tile change instead of a silent replace.
     * Basically, forge's onLoad callback isn't good enough and is sometimes
     * called before the tile has loaded from nbt, this solves that problem, by ticking
     * until it has loaded from nbt or it times out. When the tile 'times out', it
     * will log to the console then remove itself from the ticking list.
     *
     * The ticking system here should be a LAST resort, any API that allows us to
     * handle this ourself needs to be utilized.
     */
    class TileNBTContainer extends TileEntity with ITickable {
        //Store the number of ticks this tile has existed for.
        //We use this to remove the tile from the ticking list
        //after it has existed for too long.
        var ticks = 0
        //If the tile has taken too long to load.
        var failed = false
        //If the tile has successfully loaded.
        //Here just in case something weird happens,
        //we don't load it multiple times.
        var loaded = false
        //The NBT of the tile.
        //We save this back out in case something breaks.
        var tag: NBTTagCompound = _

        override def readFromNBT(t: NBTTagCompound) {
            super.readFromNBT(t)
            if (t != null) {
                tag = t.copy()
            }
        }

        override def writeToNBT(compound: NBTTagCompound) = {
            if (tag != null) {
                compound.merge(tag)
            }
            super.writeToNBT(compound)
        }

        override def update() {
            if (!failed && !loaded) {
                if (tag != null) {
                    val newTile = TileMultipart.createFromNBT(tag)
                    val chunk = world.getChunkFromBlockCoords(pos)
                    if (newTile != null) {
                        newTile.validate()
                        world.setTileEntity(pos, newTile)
                        newTile.notifyTileChange()
                        val packet = MultipartSPH.getDescPacket(chunk, Collections.singleton[TileEntity](newTile).iterator)
                        packet.sendToChunk(world, chunk.getPos.x, chunk.getPos.z)
                    } else {
                        world.setBlockToAir(pos)
                    }
                    loaded = true
                } else {
                    ticks += 1
                    if ((ticks % 600) == 0) {
                        failed = true
                        multipart.logger.warn(s"SavedMultipart at $pos still exists after $ticks ticks!")
                        world.setBlockToAir(pos)
                    }
                }
            }
        }

        override def shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newState: IBlockState) = oldState.getBlock != newState.getBlock
    }

    def load() {
        GameRegistry.registerTileEntity(classOf[TileNBTContainer], TILE_ID.toString)
    }

    def registerTileClass(t: Class[_ <: TileEntity]) {
        WrappedTileEntityRegistry.registerMapping(t, TILE_ID)
    }

    //Handle remapping tiles on chunk load.
    def loadTiles(chunk: Chunk) {
        val iterator = chunk.getTileEntityMap.entrySet().iterator()
        while (iterator.hasNext) {
            val t = iterator.next()
            t.getValue match {
                case container: TileNBTContainer =>
                    val newTile = TileMultipart.createFromNBT(container.tag)
                    if (newTile != null) {
                        newTile.setWorld(chunk.getWorld)
                        newTile.validate()
                        t.setValue(newTile)
                    } else {
                        iterator.remove()
                    }
                case _ =>
            }
        }
    }

}
