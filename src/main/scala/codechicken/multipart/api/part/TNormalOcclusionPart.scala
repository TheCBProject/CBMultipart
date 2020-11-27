package codechicken.multipart.api.part

import net.minecraft.util.math.shapes.{IBooleanFunction, VoxelShape, VoxelShapes}

/**
 * This suite of 3 classes provides simple functions for standard bounding box based occlusion testing.
 * If any two parts have overlapping bounding boxes, the test fails
 *
 * See TIconHitEffects for notes on the Scala|Java composition setup.
 */
object NormalOcclusionTest {

    /**
     * Performs the test, returns true if the test fails
     */
    def apply(part1: TNormalOcclusionPart, part2: TMultiPart): Boolean = {
        var shape = VoxelShapes.empty()
        part2 match {
            case part: TNormalOcclusionPart =>
                shape = VoxelShapes.or(shape, part.getOcclusionShape)
            case _ =>
        }
        part2 match {
            case part: TPartialOcclusionPart =>
                shape = VoxelShapes.or(shape, part.getPartialOcclusionShape)
            case _ =>
        }
        !VoxelShapes.compare(shape, part1.getOcclusionShape, IBooleanFunction.AND)
    }
}

/**
 * Trait for scala programmers
 */
trait TNormalOcclusionPart extends TMultiPart {
    /**
     * Return a list of normal occlusion boxes
     */
    def getOcclusionShape: VoxelShape

    override def occlusionTest(npart: TMultiPart): Boolean =
        NormalOcclusionTest(this, npart) && super.occlusionTest(npart)
}

/**
 * Utility part class for performing 3rd party occlusion tests
 */
class NormallyOccludedPart(shape: VoxelShape) extends TMultiPart with TNormalOcclusionPart {

    def getType = null

    def getOcclusionShape = shape
}
