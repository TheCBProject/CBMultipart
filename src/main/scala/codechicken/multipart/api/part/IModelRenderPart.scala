package codechicken.multipart.api.part

import codechicken.lib.render.CCRenderState
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.block.BlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.{BlockRendererDispatcher, RenderType}
import net.minecraft.world.ILightReader
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
        val rendererDispatcher = Minecraft.getInstance.getBlockRendererDispatcher

        val world = ccrs.lightMatrix.access //Use the ChunkRenderCache world opposed to this parts _actual_ world
        val random = new Random
        if (canRenderInLayer(layer)) {
            return rendererDispatcher.renderModel(getCurrentState, pos, world, new MatrixStack, ccrs.getConsumer, true, random, getModelData)
        }
        false
    }

    //    /**
    //     * Returns a BlockStateContainer object with all required properties for rendering. This
    //     * is called one time and cached.
    //     *
    //     * This container can contain normal and unlisted properties. The block parameter
    //     * for the container is ignored. You may use null or BlockMultipart if you wish.
    //     *
    //     * Note that this data is used for rendering only. Do not store data here. Nothing
    //     * inside these states will be saved.
    //     */
    //    def createBlockStateContainer: BlockStateContainer

    //    /**
    //     * Used to mutate the default state that is passed in into the current state of the part.
    //     * This is used for model lookup, you must set only normal properties here.
    //     * Use getExtendedState for unlisted properties.
    //     *
    //     * Note that this data is used for rendering only. Do not store data here. Nothing
    //     * inside these states will be saved.
    //     *
    //     * @param state The default state of this part
    //     * @return The current state of this part, with all properties assigned to proper values
    //     */
    //    def getCurrentState(state: IBlockState): IBlockState
    //
    //
    //    /**
    //     * Used to add Unlisted properties to the state. getCurrentState is used for model lookup.
    //     *
    //     * @param state The state from getCurrentState.
    //     * @return The state with any unlisted properties added.
    //     */
    //    def getExtendedState(state: IBlockState): IBlockState
}
