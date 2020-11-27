package codechicken.multipart.trait;

import codechicken.multipart.TileMultipart;
import codechicken.multipart.api.annotation.MultiPartTrait;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.TPartialOcclusionPart;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for the partial occlusion test.
 * <p>
 * Created by covers1624 on 2/9/20.
 */
@MultiPartTrait (TPartialOcclusionPart.class)
class TPartialOcclusionTile extends TileMultipart {

    //Static cache.
    private static TPartialOcclusionTile lastOcclusionTestedTile;
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
    public boolean occlusionTest(List<TMultiPart> parts, TMultiPart npart) {
        if (npart instanceof TPartialOcclusionPart) {
            VoxelShape newShape = ((TPartialOcclusionPart) npart).getPartialOcclusionShape();
            if (lastOcclusionTestedTile != this || lastOcclusionTestedShape != newShape) {
                lastOcclusionTestedTile = this;
                lastOcclusionTestedShape = newShape;
                lastOcclusionTestedResult = partialOcclusionTest(parts, newShape);
            }
            if (!lastOcclusionTestedResult) {
                return false;
            }
        }

        return super.occlusionTest(parts, npart);
    }

    private static boolean partialOcclusionTest(List<TMultiPart> parts, VoxelShape newShape) {
        List<VoxelShape> shapes = new ArrayList<>(parts.size() + 1);
        for (TMultiPart part : parts) {
            if (part instanceof TPartialOcclusionPart) {
                shapes.add(((TPartialOcclusionPart) part).getPartialOcclusionShape());
            }
        }
        shapes.add(newShape);

        for (VoxelShape shape1 : shapes) {
            VoxelShape uniqueShape = shape1;
            for (VoxelShape shape2 : shapes) {
                if (shape1 != shape2) {
                    uniqueShape = VoxelShapes.combine(uniqueShape, shape2, IBooleanFunction.ONLY_FIRST);
                }
            }
            if (uniqueShape.isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
