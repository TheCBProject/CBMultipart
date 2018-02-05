package codechicken.multipart

import java.lang.Iterable

import codechicken.lib.vec.Cuboid6

import scala.collection.JavaConversions._

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
    def apply(boxes1: Traversable[Cuboid6], boxes2: Traversable[Cuboid6]): Boolean =
        boxes1.forall(v1 => boxes2.forall(v2 => !v1.intersects(v2)))

    /**
     * Performs the test, returns true if the test fails
     */
    def apply(part1: TNormalOcclusionPart, part2: TMultiPart): Boolean = {
        var boxes = Seq[Cuboid6]()
        if (part2.isInstanceOf[TNormalOcclusionPart]) {
            boxes = boxes ++ part2.asInstanceOf[TNormalOcclusionPart].getOcclusionBoxes
        }

        if (part2.isInstanceOf[TPartialOcclusionPart]) {
            boxes = boxes ++ part2.asInstanceOf[TPartialOcclusionPart].getPartialOcclusionBoxes
        }

        NormalOcclusionTest(boxes, part1.getOcclusionBoxes)
    }
}

/**
 * Trait for scala programmers
 */
trait TNormalOcclusionPart extends TMultiPart {
    /**
     * Return a list of normal occlusion boxes
     */
    def getOcclusionBoxes: Iterable[Cuboid6]

    override def occlusionTest(npart: TMultiPart): Boolean =
        NormalOcclusionTest(this, npart) && super.occlusionTest(npart)
}

/**
 * Utility part class for performing 3rd party occlusion tests
 */
class NormallyOccludedPart(bounds: Iterable[Cuboid6]) extends TMultiPart with TNormalOcclusionPart {
    def this(bound: Cuboid6) = this(Seq(bound))

    def getType = null

    def getOcclusionBoxes = bounds
}
