package codechicken.multipart

import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.Vector3
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
 * Used for mass TESR Batching.
 *
 * FastTESR is a construct provided by forge for batching
 * large amounts of dynamic rendering together when they
 * don't necessarily need different VertexFormats. Keep
 * in mind, Using this means you are locked to
 * [[net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK]]
 * if that wont work for your application use [[TDynamicRenderPart]]
 *
 * Unlike forge, FMP allows you to have your part
 * render with both dynamic and fast rendering.
 *
 * It is recommenced you use CCRenderState and CCModels
 * with Matrix transformations in order to achieve the
 * best efficiency
 */
trait TFastRenderPart extends TMultiPart with TTESRPart {

    /**
     * If your part can render fast on the specified pass.
     *
     * @param pass The render pass.
     * @return If you render fast on this layer.
     */
    def canRenderFast(pass:Int) = false


    /**
     * Render the dynamic, changing faces of this part as in a FastTESR. You can
     * use this only if you don't need to mess with the GL state. Otherwise, use
     * [[TDynamicRenderPart]] This is only called if canRenderFast returns true.
     *
     *
     * CCRenderState is set up as follows should you wish to use it:
     *  - CCRenderState.reset() has been called
     *  - The current buffer is bound
     *
     *  Otherwise an instance of the VertexBuffer can be retrieved from
     *  CCRenderState via CCRenderState.getBuffer()
     *
     * NOTE: The tessellator is already drawing. DO NOT make draw calls or
     *       mess with the GL state
     *
     *
     * @param ccrs The instance of CCRenderState to use.
     * @param pos The position of this block space relative to the renderer, same as x, y, z passed to TESR.
     * @param pass The render pass, 1 or 0
     * @param frameDelta The partial interpolation frame value for animations between ticks
     */
    @SideOnly(Side.CLIENT)
    def renderFast(ccrs:CCRenderState, pos:Vector3, pass:Int, frameDelta:Float){}

}
