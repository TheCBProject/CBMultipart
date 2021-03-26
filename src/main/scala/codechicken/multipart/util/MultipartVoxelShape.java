package codechicken.multipart.util;

import codechicken.lib.raytracer.DistanceRayTraceResult;
import codechicken.multipart.block.TileMultiPart;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.shapes.VoxelShape;

import java.util.Objects;

/**
 * Created by covers1624 on 3/9/20.
 */
public class MultipartVoxelShape extends VoxelShape {

    private final VoxelShape shape;
    private final TileMultiPart tile;

    public MultipartVoxelShape(VoxelShape shape, TileMultiPart tile) {
        super(shape.shape);
        this.shape = shape;
        this.tile = tile;
    }

    @Override
    public DoubleList getCoords(Direction.Axis axis) {
        return shape.getCoords(axis);
    }

    @Override
    public BlockRayTraceResult clip(Vector3d start, Vector3d end, BlockPos pos) {

        return tile.getPartList().stream()
                .map(part -> {
                    BlockRayTraceResult hit = part.getInteractionShape().clip(start, end, pos);
                    if (hit == null) {
                        hit = part.getOutlineShape().clip(start, end, pos);
                    }
                    if (hit == null) {
                        return null;
                    }
                    PartRayTraceResult result;
                    if (hit instanceof DistanceRayTraceResult) {
                        result = new PartRayTraceResult(part, (DistanceRayTraceResult) hit);
                    } else {
                        result = new PartRayTraceResult(part, hit, start);
                    }
                    result.subHit = hit.subHit;
                    result.hitInfo = hit.hitInfo;
                    return result;
                })
                .filter(Objects::nonNull)
                .sorted()
                .findFirst()
                .orElse(null);
    }
}
