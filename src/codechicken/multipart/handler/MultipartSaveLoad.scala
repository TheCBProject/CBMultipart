package codechicken.multipart.handler

import codechicken.multipart.MultipartHelper.IPartTileConverter
import codechicken.multipart.{MultipartHelper, TileMultipart, WrappedTileEntityRegistry}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.fml.common.registry.GameRegistry

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * Hack due to lack of TileEntityLoadEvent in forge
 */
object MultipartSaveLoad
{

    val TILE_ID: ResourceLocation = new ResourceLocation("fmp:savedmultipart")

    val converters = mutable.MutableList[IPartTileConverter[_]]()
    var loadingWorld: World = _

    class TileNBTContainer extends TileEntity
    {
        var tag: NBTTagCompound = _

        override def readFromNBT(t: NBTTagCompound)
        {
            super.readFromNBT(t)
            tag = t
        }

        def fmp_handleLoad(world: World):Unit = {
            handleTileLoad(this, world)
        }
    }
    def load(): Unit = {

        GameRegistry.registerTileEntity(classOf[TileNBTContainer], TILE_ID.toString)
    }

    def registerTileClass(t: Class[_ <: TileEntity])
    {
        WrappedTileEntityRegistry.registerMapping(t, TILE_ID)
    }

    def loadTiles(chunk: Chunk)
    {
        loadingWorld = chunk.getWorld
        val iterator = chunk.getTileEntityMap.entrySet.iterator
        while (iterator.hasNext) {
            val e = iterator.next
            var next = false
            val t = e.getValue match {
                case t:TileNBTContainer if t.tag.getString("id") == TILE_ID.toString =>
                    TileMultipart.createFromNBT(e.getValue.asInstanceOf[TileNBTContainer].tag)
                case t => converters.find(_.canConvert(t)) match {
                    case Some(c) =>
                        val parts = c.convert(t)
                        if(parts.nonEmpty) MultipartHelper.createTileFromParts(parts)
                        else null
                    case _ =>
                        next = true
                        null
                }
            }

            if(!next) {
                if (t != null) {
                    t.setWorld(e.getValue.getWorld)
                    t.validate()
                    e.setValue(t)
                }
                else iterator.remove()
            }
        }
    }

    def handleTileLoad(tile: TileEntity, world: World): TileEntity = {
        val t = tile match {
            case t:TileNBTContainer if t.tag.getString("id") == TILE_ID.toString =>
                TileMultipart.createFromNBT(t.tag)
            case t => converters.find(_.canConvert(t)) match {
                case Some(c) =>
                    val parts = c.convert(t)
                    if(parts.nonEmpty) MultipartHelper.createTileFromParts(parts)
                    else null
                case _ =>
                    null
            }
        }

        if (t != null) {
            world.setTileEntity(t.getPos, t)
        }
        t
    }
}
