package codechicken.multipart.api;

import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.TNormalOcclusionPart;
import codechicken.multipart.api.part.TPartialOcclusionPart;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

/**
 * Utilities for performing a 'normal' occlusion test, where no Shape may obstruct the other in any capacity.
 */
public class NormalOcclusionTest {

    /**
     * Test if part1 is occluded by part2 in any way.
     * <p>
     * Checks both {@link TNormalOcclusionPart#getOcclusionShape()} and
     * {@link TPartialOcclusionPart#getPartialOcclusionShape()} on part2.
     *
     * @param part1 The first part.
     * @param part2 The other part.
     * @return If part1 is occluded by part2 in any way.
     */
    public static boolean test(TNormalOcclusionPart part1, TMultiPart part2) {
        VoxelShape shape = VoxelShapes.empty();
        if (part2 instanceof TNormalOcclusionPart) {
            shape = VoxelShapes.or(shape, ((TNormalOcclusionPart) part2).getOcclusionShape());
        }
        if (part2 instanceof TPartialOcclusionPart) {
            shape = VoxelShapes.or(shape, ((TPartialOcclusionPart) part2).getPartialOcclusionShape());
        }

        return !VoxelShapes.compare(shape, part1.getOcclusionShape(), IBooleanFunction.AND);
    }

    /**
     * Wraps the given {@link VoxelShape} to a {@link TNormalOcclusionPart} for the purpose of occlusion testing.
     *
     * @param shape The shape.
     * @return The wrapped VoxelShape part.
     */
    public static TNormalOcclusionPart of(VoxelShape shape) {
        return new NormallyOccludedPart(shape);
    }

    private static class NormallyOccludedPart extends TMultiPart implements TNormalOcclusionPart {

        private final VoxelShape shape;

        private NormallyOccludedPart(VoxelShape shape) {
            this.shape = shape;
        }

        @Override
        public MultiPartType<?> getType() {
            return null;
        }

        @Override
        public VoxelShape getOcclusionShape() {
            return shape;
        }
    }
}
