package codechicken.multipart.handler

import codechicken.multipart.{TileMultipart, WrappedTileEntityRegistry}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.GameRegistry

/**
 * Contains the holder tile that is loaded from NBT.
 * The holder tile saves the nbt from readFromNBT and uses it in onLoad to replace the tile in world with the ASM generated composite tile.
 */
object MultipartSaveLoad {

    val TILE_ID: ResourceLocation = new ResourceLocation("ccmp:saved_multipart")

    class TileNBTContainer extends TileEntity {
        var tag: NBTTagCompound = _

        override def readFromNBT(t: NBTTagCompound) {
            super.readFromNBT(t)
            tag = t
        }

        override def writeToNBT(compound: NBTTagCompound) = super.writeToNBT(tag)

        override def onLoad() {
            val newTile = TileMultipart.createFromNBT(tag, world)
            newTile.validate()
            world.setTileEntity(pos, newTile)
        }
    }

    def load() {
        GameRegistry.registerTileEntity(classOf[TileNBTContainer], TILE_ID.toString)
    }

    def registerTileClass(t: Class[_ <: TileEntity]) {
        WrappedTileEntityRegistry.registerMapping(t, TILE_ID)
    }
}
