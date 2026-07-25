package codechicken.multipart.util;

import codechicken.lib.raytracer.SubHitBlockHitResult;
import codechicken.multipart.api.part.MultiPart;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A {@link BlockHitResult} which has hit a specific part.
 * <p>
 * Created by covers1624 on 2/9/20.
 */
public class PartRayTraceResult extends BlockHitResult implements Comparable<SubHitBlockHitResult> {

    /**
     * The shape of the part that was hit.
     */
    public final VoxelShape hitShape;
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
        super(hit.getType() == Type.MISS, hit.getLocation(), hit.getDirection(), hit.getBlockPos(), hit.isInside(), hit.isWorldBorderHit());
        this.hitShape = hitShape;
        this.part = part;
        this.distance = hit.getLocation().distanceToSqr(start);
        this.hit = hit;
    }

    @Override
    public int compareTo(SubHitBlockHitResult o) {
        return Double.compare(distance, o.dist);
    }
}
