package codechicken.multipart

import codechicken.lib.vec.Vector3
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
 * Trait used to mark a part as needing Dynamic rendering.
 * Use this if you need to modify any GL state
 * Such as, blend, depth and so on.
 * If you only require use of transformations, you should
 * be using [[TFastRenderPart]], See there for more info.
 *
 * Created by covers1624 on 21/01/2018.
 */
trait TDynamicRenderPart extends TMultiPart with TTESRPart {

    /**
     * If true your part wishes to use dynamic rendering on this pass.
     * Note: If you don't need to touch GL, it is recommended to use [[TFastRenderPart]]
     *
     * @param pass The render pass.
     * @return If dynamic rendering is needed.
     */
    def canRenderDynamic(pass: Int) = false


    /**
     * Render the dynamic, changing faces of this part and other gfx as in a TESR.
     * If you do not need to mess with GL states, you can use the renderFast method
     * instead. This method is only called if [[canRenderDynamic]] returns true.
     *
     * CCRenderState is set up as follows should you wish to use it:
     *  - CCRenderState.reset() has been called
     *  - The current buffer is bound
     *
     * NOTE: The tessellator is not drawing. You need to start it and make draw calls
     * if it is to be used.
     *
     * @param pos   The position of this block space relative to the renderer, same as x, y, z passed to TESR.
     * @param pass  The render pass, 1 or 0
     * @param frameDelta The partial interpolation frame value for animations between ticks
     */
    @SideOnly(Side.CLIENT)
    def renderDynamic(pos: Vector3, pass: Int, frameDelta: Float) {}
}
