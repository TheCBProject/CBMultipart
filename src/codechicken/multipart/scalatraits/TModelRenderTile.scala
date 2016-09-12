package codechicken.multipart.scalatraits

import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.Vector3
import codechicken.multipart._
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.BlockRenderLayer
import net.minecraftforge.client.ForgeHooksClient

import scala.collection.mutable.ListBuffer

/**
  * Mixin implementation for IModelRenderPart.
  */
trait TModelRenderTile extends TileMultipartClient
{
    private var modelPartList = ListBuffer[IModelRenderPart]()

    override def copyFrom(that:TileMultipart)
    {
        super.copyFrom(that)
        that match {
            case mt:TModelRenderTile => modelPartList = mt.modelPartList
            case _ =>
        }
    }

    override def bindPart(part:TMultiPart)
    {
        super.bindPart(part)
        part match {
            case mp:IModelRenderPart => modelPartList += mp
            case _ =>
        }
    }

    override def clearParts()
    {
        super.clearParts()
        modelPartList.clear()
    }

    override def partRemoved(part:TMultiPart, p:Int)
    {
        super.partRemoved(part, p)
        part match {
            case mp:IModelRenderPart => modelPartList -= mp
            case _ =>
        }
    }

    override def renderStatic(pos:Vector3, layer:BlockRenderLayer) =
    {
        var r = super.renderStatic(pos, layer)

        renderModel(modelPartList.filter(_.canRenderInLayer(layer)), {(model, state) =>
            Minecraft.getMinecraft.getBlockRendererDispatcher.getBlockModelRenderer
                    .renderModel(getWorld, model, state, getPos, CCRenderState.getBuffer, true)
            r |= true
        })

        r
    }

    override def renderDamage(pos:Vector3, texture:TextureAtlasSprite)
    {
        super.renderDamage(pos, texture)

        Minecraft.getMinecraft.objectMouseOver match {
            case hit:PartRayTraceResult => partList(hit.partIndex) match {
                case p:IModelRenderPart =>
                    renderModel(ListBuffer(p), {(model, state) =>
                        val dm = ForgeHooksClient.getDamageModel(model, texture, state, getWorld, getPos)
                        Minecraft.getMinecraft.getBlockRendererDispatcher.getBlockModelRenderer
                                .renderModel(getWorld, dm, state, getPos, CCRenderState.getBuffer, true)
                    })
                case _ =>
            }
            case _ =>
        }
    }

    private def renderModel(list:ListBuffer[IModelRenderPart], f:(IBakedModel, IBlockState) => Unit)
    {
        import MultiPartRegistryClient._
        for (mp <- list) {
            val container = getModelPartContainer(mp)
            val state = mp.getCurrentState(container.getBaseState)
            val disp = Minecraft.getMinecraft.getBlockRendererDispatcher

            disp.getBlockModelShapes.getModelForState(state) match {
                case null =>
                case model => f(model, state)
            }
        }
    }
}