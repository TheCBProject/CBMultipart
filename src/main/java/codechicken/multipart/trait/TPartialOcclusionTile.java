package codechicken.multipart.trait;

import codechicken.multipart.api.annotation.MultiPartTrait;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.TPartialOcclusionPart;
import codechicken.multipart.block.TileMultiPart;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation for the partial occlusion test.
 * <p>
 * Created by covers1624 on 2/9/20.
 */
@MultiPartTrait (TPartialOcclusionPart.class)
public class TPartialOcclusionTile extends TileMultiPart {

    //Static cache.
    @Nullable
    private static TPartialOcclusionTile lastOcclusionTestedTile;
    @Nullable
    private static VoxelShape lastOcclusionTestedShape;
    private static boolean lastOcclusionTestedResult;

    @Override
    public void bindPart(TMultiPart newPart) {
        super.bindPart(newPart);

        if (newPart instanceof TPartialOcclusionPart && lastOcclusionTestedTile == this) {
            lastOcclusionTestedTile = null;
        }
    }

    @Override
    public void partRemoved(TMultiPart remPart, int p) {
        super.partRemoved(remPart, p);

        if (remPart instanceof TPartialOcclusionPart && lastOcclusionTestedTile == this) {
            lastOcclusionTestedTile = null;
        }
    }

    @Override
    public boolean occlusionTest(Iterable<TMultiPart> parts, TMultiPart npart) {
        if (npart instanceof TPartialOcclusionPart newPart) {
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

    private static boolean partialOcclusionTest(Iterable<TMultiPart> allParts, TPartialOcclusionPart newPart) {
        List<TPartialOcclusionPart> parts = new LinkedList<>();
        for (TMultiPart part : allParts) {
            if (part instanceof TPartialOcclusionPart) {
                parts.add((TPartialOcclusionPart) part);
            }
        }
        parts.add(newPart);

        for (TPartialOcclusionPart part1 : parts) {
            if (part1.allowCompleteOcclusion()) {
                continue;
            }
            VoxelShape uniqueShape = part1.getPartialOcclusionShape();
            for (TPartialOcclusionPart part2 : parts) {
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
