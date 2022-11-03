package codechicken.multipart.api.part;

import codechicken.multipart.api.NormalOcclusionTest;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Provides standard bounding box based occlusion testing.
 * If any two parts have overlapping bounding boxes, the test fails
 */
public interface NormalOcclusionPart extends MultiPart {

    VoxelShape getOcclusionShape();

    @Override
    default boolean occlusionTest(MultiPart nPart) {
        return NormalOcclusionTest.test(this, nPart);
    }
}
