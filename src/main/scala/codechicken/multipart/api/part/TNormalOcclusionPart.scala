package codechicken.multipart.api.part

import codechicken.multipart.api.NormalOcclusionTest
import net.minecraft.util.math.shapes.{IBooleanFunction, VoxelShape, VoxelShapes}

/**
 * Provides standard bounding box based occlusion testing.
 * If any two parts have overlapping bounding boxes, the test fails
 */
trait TNormalOcclusionPart extends TMultiPart {
    /**
     * Return a list of normal occlusion boxes
     */
    def getOcclusionShape: VoxelShape

    override def occlusionTest(npart: TMultiPart): Boolean =
        NormalOcclusionTest.test(this, npart) && super.occlusionTest(npart)
}
