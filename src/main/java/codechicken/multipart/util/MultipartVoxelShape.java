package codechicken.multipart.util;

import codechicken.lib.raytracer.SubHitBlockHitResult;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.TileMultiPart;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

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
    public BlockHitResult clip(Vec3 start, Vec3 end, BlockPos pos) {

        PartRayTraceResult closest = null;
        for (TMultiPart part : tile.getPartList()) {
            BlockHitResult hit = part.getInteractionShape().clip(start, end, pos);
            if (hit == null) {
                hit = part.getShape(CollisionContext.empty()).clip(start, end, pos);
            }
            if (hit == null) continue;

            PartRayTraceResult result;
            if (hit instanceof SubHitBlockHitResult sHit) {
                result = new PartRayTraceResult(part, sHit);
            } else {
                result = new PartRayTraceResult(part, hit, start);
            }
            if (closest == null || result.compareTo(closest) < 0) {
                closest = result;
            }
        }
        return closest;
    }
}
