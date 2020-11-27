package codechicken.multipart.util;

import codechicken.lib.raytracer.DistanceRayTraceResult;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.api.part.TMultiPart;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Created by covers1624 on 3/9/20.
 */
public class MultipartVoxelShape extends VoxelShape {

    private final VoxelShape shape;
    private final TileMultipart tile;

    public MultipartVoxelShape(VoxelShape shape, TileMultipart tile) {
        super(shape.part);
        this.shape = shape;
        this.tile = tile;
    }

    @Override
    public DoubleList getValues(Direction.Axis axis) {
        return shape.getValues(axis);
    }

    @Override
    public BlockRayTraceResult rayTrace(Vec3d start, Vec3d end, BlockPos pos) {

        List<TMultiPart> parts = tile.jPartList();
        return IntStream.range(0, parts.size())//
                .mapToObj(index -> {
                    TMultiPart part = parts.get(index);
                    BlockRayTraceResult hit = part.getRayTraceShape().rayTrace(start, end, pos);
                    if (hit == null) {
                        hit = part.getOutlineShape().rayTrace(start, end, pos);
                    }
                    if (hit == null) {
                        return null;
                    }
                    PartRayTraceResult result;
                    if (hit instanceof DistanceRayTraceResult) {
                        result = new PartRayTraceResult(index, (DistanceRayTraceResult) hit);
                    } else {
                        result = new PartRayTraceResult(index, hit, start);
                    }
                    result.subHit = hit.subHit;
                    result.hitInfo = hit.hitInfo;
                    return result;
                })//
                .filter(Objects::nonNull)//
                .sorted()//
                .findFirst()//
                .orElse(null);
    }
}
