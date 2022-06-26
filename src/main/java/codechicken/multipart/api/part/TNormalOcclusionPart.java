package codechicken.multipart.api.part;

import codechicken.multipart.api.NormalOcclusionTest;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Provides standard bounding box based occlusion testing.
 * If any two parts have overlapping bounding boxes, the test fails
 */
public interface TNormalOcclusionPart extends TMultiPart {

    VoxelShape getOcclusionShape();

    @Override
    default boolean occlusionTest(TMultiPart npart) {
        return NormalOcclusionTest.test(this, npart);
    }
}
