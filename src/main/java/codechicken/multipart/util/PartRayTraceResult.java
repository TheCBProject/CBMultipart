package codechicken.multipart.util;

import codechicken.lib.raytracer.SubHitBlockHitResult;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.api.part.MultiPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Created by covers1624 on 2/9/20.
 */
public class PartRayTraceResult extends SubHitBlockHitResult {

    public final MultiPart part;

    public PartRayTraceResult(MultiPart part, BlockHitResult hit, Vec3 start) {
        this(part, new Vector3(hit.getLocation()), hit.getDirection(), hit.getBlockPos(), hit.isInside(), null, hit.getLocation().distanceToSqr(start));
    }

    public PartRayTraceResult(MultiPart part, SubHitBlockHitResult hit) {
        this(part, new Vector3(hit.getLocation()), hit.getDirection(), hit.getBlockPos(), hit.isInside(), hit.hitInfo, hit.dist);
    }

    public PartRayTraceResult(MultiPart part, Vector3 hitVec, Direction faceIn, BlockPos posIn, boolean isInside, Object data, double dist) {
        super(hitVec, faceIn, posIn, isInside, data, dist);
        this.part = part;
    }
}
