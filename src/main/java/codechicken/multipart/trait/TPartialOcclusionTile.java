package codechicken.multipart.trait;

import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.PartialOcclusionPart;
import codechicken.multipart.block.TileMultipart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Implementation for the partial occlusion test.
 * <p>
 * Created by covers1624 on 2/9/20.
 */
public class TPartialOcclusionTile extends TileMultipart {

    // Cached partial occlusion test results for performance
    // This cache exists almost entirely for the microblock highlight renderer, due to how expensive combining VoxelShapes is.
    // Normal occlusion operations happen infrequently enough that this is not a performance concern during normal gameplay.
    // TODO, Figure out how to cleanly nuke this and do caching inside MicroblockRender.
    @Nullable
    private static Iterable<MultiPart> lastTestParts = null;
    @Nullable
    private static VoxelShape lastTestShape = null;
    private static boolean lastTestResult = false;

    public TPartialOcclusionTile(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public boolean occlusionTest(Iterable<MultiPart> parts, MultiPart npart) {
        if (npart instanceof PartialOcclusionPart newPart) {
            VoxelShape newShape = newPart.getPartialOcclusionShape();
            if (!getLevel().isClientSide() || lastTestParts != parts || lastTestShape != newShape) {
                lastTestParts = parts;
                lastTestShape = newShape;
                lastTestResult = partialOcclusionTest(parts, newPart);
            }
            if (!lastTestResult) {
                return false;
            }
        }

        return super.occlusionTest(parts, npart);
    }

    private static boolean partialOcclusionTest(Iterable<MultiPart> allParts, PartialOcclusionPart newPart) {
        List<PartialOcclusionPart> parts = new LinkedList<>();
        for (MultiPart part : allParts) {
            if (part instanceof PartialOcclusionPart) {
                parts.add((PartialOcclusionPart) part);
            }
        }
        parts.add(newPart);

        for (PartialOcclusionPart part1 : parts) {
            if (part1.allowCompleteOcclusion()) {
                continue;
            }
            VoxelShape uniqueShape = part1.getPartialOcclusionShape();
            for (PartialOcclusionPart part2 : parts) {
                if (part1 != part2) {
                    uniqueShape = Shapes.join(uniqueShape, part2.getPartialOcclusionShape(), BooleanOp.ONLY_FIRST);
                }
            }
            if (uniqueShape.isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
