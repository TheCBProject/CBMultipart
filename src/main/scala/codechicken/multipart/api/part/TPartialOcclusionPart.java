package codechicken.multipart.api.part;

import codechicken.multipart.api.annotation.MultiPartMarker;
import codechicken.multipart.trait.TPartialOcclusionTile;
import net.minecraft.util.math.shapes.VoxelShape;

/**
 * This class provides a special type of occlusion model used by microblocks.
 * The partial occlusion test defines bounding boxes that may intersect, so long as no part is completely obscured by a combination of the others.
 * Partial bounding boxes may not intersect with normal bounding boxes from {@link TNormalOcclusionPart}
 * <p>
 * This part marker is managed by the mixin trait {@link codechicken.multipart.trait.TPartialOcclusionTile}.
 */
@MultiPartMarker (TPartialOcclusionTile.class)
public interface TPartialOcclusionPart {

    /**
     * The VoxelShape to use for Partial occlusion tests,
     * this shape must not be occluded by any other {@link TPartialOcclusionPart}'s shape,
     * unless {@link #allowCompleteOcclusion()} returns true.
     * <p>
     * It is expected that this method return some form of cached instance that does NOT change
     * each call, unless some internal state has changed.
     *
     * @return the VoxelShape for partial occlusion tests.
     */
    VoxelShape getPartialOcclusionShape();

    /**
     * Return true if this part may be completely obscured
     */
    default boolean allowCompleteOcclusion() {
        return false;
    }
}
