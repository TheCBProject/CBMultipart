package codechicken.multipart.util;

import codechicken.lib.raytracer.DistanceRayTraceResult;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.api.part.TMultiPart;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;

/**
 * Created by covers1624 on 2/9/20.
 */
public class PartRayTraceResult extends DistanceRayTraceResult {

    public final TMultiPart part;

    public PartRayTraceResult(TMultiPart part, BlockRayTraceResult hit, Vec3d start) {
        this(part, new Vector3(hit.getHitVec()), hit.getFace(), hit.getPos(), hit.isInside(), hit.hitInfo, hit.getHitVec().squareDistanceTo(start));
    }

    public PartRayTraceResult(TMultiPart part, DistanceRayTraceResult hit) {
        this(part, new Vector3(hit.getHitVec()), hit.getFace(), hit.getPos(), hit.isInside(), hit.hitInfo, hit.dist);
    }

    public PartRayTraceResult(TMultiPart part, Vector3 hitVec, Direction faceIn, BlockPos posIn, boolean isInside, Object data, double dist) {
        super(hitVec, faceIn, posIn, isInside, data, dist);
        this.part = part;
    }
}
