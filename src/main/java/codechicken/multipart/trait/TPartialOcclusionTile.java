package codechicken.multipart.trait;

import codechicken.multipart.api.annotation.MultiPartTrait;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.PartialOcclusionPart;
import codechicken.multipart.block.TileMultipart;
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
@MultiPartTrait (PartialOcclusionPart.class)
public class TPartialOcclusionTile extends TileMultipart {

    //Static cache.
    @Nullable
    private static TPartialOcclusionTile lastOcclusionTestedTile;
    @Nullable
    private static VoxelShape lastOcclusionTestedShape;
    private static boolean lastOcclusionTestedResult;

    @Override
    public void bindPart(MultiPart newPart) {
        super.bindPart(newPart);

        if (newPart instanceof PartialOcclusionPart && lastOcclusionTestedTile == this) {
            lastOcclusionTestedTile = null;
        }
    }

    @Override
    public void partRemoved(MultiPart remPart, int p) {
        super.partRemoved(remPart, p);

        if (remPart instanceof PartialOcclusionPart && lastOcclusionTestedTile == this) {
            lastOcclusionTestedTile = null;
        }
    }

    @Override
    public boolean occlusionTest(Iterable<MultiPart> parts, MultiPart npart) {
        if (npart instanceof PartialOcclusionPart newPart) {
            VoxelShape newShape = newPart.getPartialOcclusionShape();
            if (lastOcclusionTestedTile != this || lastOcclusionTestedShape != newShape) {
                lastOcclusionTestedTile = this;
                lastOcclusionTestedShape = newShape;
                lastOcclusionTestedResult = partialOcclusionTest(parts, newPart);
            }
            if (!lastOcclusionTestedResult) {
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
