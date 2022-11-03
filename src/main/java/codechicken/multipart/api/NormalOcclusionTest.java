package codechicken.multipart.api;

import codechicken.multipart.api.part.BaseMultipart;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.NormalOcclusionPart;
import codechicken.multipart.api.part.PartialOcclusionPart;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Utilities for performing a 'normal' occlusion test, where no Shape may obstruct the other in any capacity.
 */
public class NormalOcclusionTest {

    /**
     * Test if part1 is occluded by part2 in any way.
     * <p>
     * Checks both {@link NormalOcclusionPart#getOcclusionShape()} and
     * {@link PartialOcclusionPart#getPartialOcclusionShape()} on part2.
     *
     * @param part1 The first part.
     * @param part2 The other part.
     * @return If part1 is occluded by part2 in any way.
     */
    public static boolean test(NormalOcclusionPart part1, MultiPart part2) {
        VoxelShape shape = Shapes.empty();
        if (part2 instanceof NormalOcclusionPart p) {
            shape = Shapes.or(shape, p.getOcclusionShape());
        }
        if (part2 instanceof PartialOcclusionPart p) {
            shape = Shapes.or(shape, p.getPartialOcclusionShape());
        }

        return !Shapes.joinIsNotEmpty(shape, part1.getOcclusionShape(), BooleanOp.AND);
    }

    /**
     * Wraps the given {@link VoxelShape} to a {@link NormalOcclusionPart} for the purpose of occlusion testing.
     *
     * @param shape The shape.
     * @return The wrapped VoxelShape part.
     */
    public static NormalOcclusionPart of(VoxelShape shape) {
        return new NormallyOccludedPart(shape);
    }

    private static class NormallyOccludedPart extends BaseMultipart implements NormalOcclusionPart {

        private final VoxelShape shape;

        private NormallyOccludedPart(VoxelShape shape) {
            this.shape = shape;
        }

        @Override
        public MultipartType<?> getType() {
            // Yes, this returns null, however should only be used
            // inside the occlusion engine, making this safe.
            //noinspection ConstantConditions
            return null;
        }

        @Override
        public VoxelShape getOcclusionShape() {
            return shape;
        }
    }
}
