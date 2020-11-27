package codechicken.multipart.util;

import codechicken.lib.raytracer.DistanceRayTraceResult;
import codechicken.lib.vec.Vector3;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;

/**
 * Created by covers1624 on 2/9/20.
 */
public class PartRayTraceResult extends DistanceRayTraceResult {

    public final int partIndex;

    public PartRayTraceResult(int partIndex, BlockRayTraceResult hit, Vec3d start) {
        this(partIndex, new Vector3(hit.getHitVec()), hit.getFace(), hit.getPos(), hit.isInside(), hit.hitInfo, hit.getHitVec().squareDistanceTo(start));
    }

    public PartRayTraceResult(int partIndex, DistanceRayTraceResult hit) {
        this(partIndex, new Vector3(hit.getHitVec()), hit.getFace(), hit.getPos(), hit.isInside(), hit.hitInfo, hit.dist);
    }

    public PartRayTraceResult(int partIndex, Vector3 hitVec, Direction faceIn, BlockPos posIn, boolean isInside, Object data, double dist) {
        super(hitVec, faceIn, posIn, isInside, data, dist);
        this.partIndex = partIndex;
    }
}
