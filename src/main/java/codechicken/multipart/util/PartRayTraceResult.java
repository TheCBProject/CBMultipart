package codechicken.multipart.util;

import codechicken.lib.raytracer.SubHitBlockHitResult;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.api.part.MultiPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link BlockHitResult} which has hit a specific part.
 * <p>
 * Created by covers1624 on 2/9/20.
 */
// TODO, 1.21.4+, Don't implement SubHitBlockHitResult
public class PartRayTraceResult extends SubHitBlockHitResult implements Comparable<SubHitBlockHitResult> {

    /**
     * The shape of the part that was hit.
     */
    // TODO remove @Nullable in 1.21.4+
    public final @Nullable VoxelShape hitShape;
    /**
     * The part that was hit.
     */
    public final MultiPart part;
    /**
     * The square distance from the ray trace start location to the hit point.
     */
    public final double distance;
    /**
     * The raw {@link BlockHitResult} that the part's shape returned.
     */
    public final BlockHitResult hit;

    public PartRayTraceResult(VoxelShape hitShape, MultiPart part, BlockHitResult hit, Vec3 start) {
        super(
                new Vector3(hit.getLocation()),
                hit.getDirection(),
                hit.getBlockPos(),
                hit.isInside(),
                hit instanceof SubHitBlockHitResult sHit ? sHit.hitInfo : null,
                hit.getLocation().distanceToSqr(start)
        );
        this.hitShape = hitShape;
        this.part = part;
        this.distance = hit.getLocation().distanceToSqr(start);
        this.hit = hit;
    }

    @Deprecated (forRemoval = true)
    public PartRayTraceResult(MultiPart part, SubHitBlockHitResult hit) {
        super(new Vector3(hit.getLocation()), hit.getDirection(), hit.getBlockPos(), hit.isInside(), hit.hitInfo, hit.dist);
        this.part = part;
        this.distance = hit.dist;
        this.hit = hit;
        this.hitShape = null;
    }

    @Deprecated (forRemoval = true)
    public PartRayTraceResult(MultiPart part, Vector3 hitVec, Direction faceIn, BlockPos posIn, boolean isInside, Object data, double dist) {
        super(hitVec, faceIn, posIn, isInside, data, dist);
        this.part = part;
        this.distance = dist;
        this.hit = new BlockHitResult(hitVec.vec3(), faceIn, posIn, isInside);
        this.hitShape = null;
    }

    @Override
    public int compareTo(SubHitBlockHitResult o) {
        return Double.compare(distance, o.dist);
    }
}
