package codechicken.multipart

import codechicken.lib.vec.Cuboid6

/**
 * Trait used for common Fast and Dynamic render part methods.
 * Implementing this has absolutely no effect, See [[TDynamicRenderPart]] and [[TFastRenderPart]].
 */
trait TTESRPart extends TMultiPart {

    /**
     * @return A Cuboid6 bounding the render of this part for frustum culling. The bounds are relative to the tile coordinates.
     */
    def getRenderBounds = Cuboid6.full

}
