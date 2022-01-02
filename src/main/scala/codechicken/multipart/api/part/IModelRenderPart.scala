package codechicken.multipart.api.part

import codechicken.lib.render.CCRenderState
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.block.BlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.{BlockRendererDispatcher, RenderType}
import net.minecraftforge.client.model.data.IModelData

import java.util.Random

/**
 * Marker Interface for parts that wish to use vanilla-like models. Register an instance
 * of this part in MultipartRegistryClient for custom state mapping if needed.
 *
 * Note that the standard render methods (renderStatic, renderDynamic/renderFast) will
 * still be called should you wish to render portions of this part that way.
 */
trait IModelRenderPart extends TMultiPart {

    /**
     * Used to determine if this part should be rendered in
     * the layer.
     */
    def canRenderInLayer(layer: RenderType): Boolean

    def getCurrentState: BlockState

    def getModelData: IModelData

    override def renderStatic(layer: RenderType, ccrs: CCRenderState): Boolean = {
        val rendererDispatcher = Minecraft.getInstance.getBlockRenderer

        val world = ccrs.lightMatrix.access //Use the ChunkRenderCache world opposed to this parts _actual_ world
        val random = new Random
        if (canRenderInLayer(layer)) {
            return rendererDispatcher.renderModel(getCurrentState, pos, world, new MatrixStack, ccrs.getConsumer, true, random, getModelData)
        }
        false
    }
}
