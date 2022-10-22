package codechicken.microblock.part.face;

import codechicken.lib.vec.Line3;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.part.PlacementGrid;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.covers1624.quack.util.LazyValue;

import java.util.List;

/**
 * Created by covers1624 on 20/10/22.
 */
public class FaceEdgeGrid extends PlacementGrid {

    public static final FaceEdgeGrid FACE_PLACEMENT_GRID = new FaceEdgeGrid(1 / 4D);
    public static final FaceEdgeGrid HOLLOW_PLACEMENT_GRID = new FaceEdgeGrid(3 / 8D);

    private final double size;
    private final LazyValue<List<Line3>> lines;

    public FaceEdgeGrid(double size) {
        this.size = size;
        lines = new LazyValue<>(() -> List.of(
                new Line3(-0.5, 0, -0.5, -0.5, 0, 0.5),
                new Line3(-0.5, 0, 0.5, 0.5, 0, 0.5),
                new Line3(0.5, 0, 0.5, 0.5, 0, -0.5),
                new Line3(0.5, 0, -0.5, -0.5, 0, -0.5),
                new Line3(0.5, 0, 0.5, size, 0, size),
                new Line3(-0.5, 0, 0.5, -size, 0, size),
                new Line3(0.5, 0, -0.5, size, 0, -size),
                new Line3(-0.5, 0, -0.5, -size, 0, -size),
                new Line3(-size, 0, -size, -size, 0, size),
                new Line3(-size, 0, size, size, 0, size),
                new Line3(size, 0, size, size, 0, -size),
                new Line3(size, 0, -size, -size, 0, -size)
        ));
    }

    @Override
    public List<Line3> getOverlayLines() {
        return lines.get();
    }

    @Override
    public int getHitSlot(Vector3 vHit, int side) {
        int s1 = (side + 2) % 6;
        int s2 = (side + 4) % 6;
        double u = vHit.copy().add(-0.5, -0.5, -0.5).scalarProject(Rotation.axes[s1]);
        double v = vHit.copy().add(-0.5, -0.5, -0.5).scalarProject(Rotation.axes[s2]);

        if (Math.abs(u) < size && Math.abs(v) < size) {
            return side ^ 1;
        }
        if (Math.abs(u) > Math.abs(v)) {
            return u > 0 ? s1 : s1 ^ 1;
        }
        return v > 0 ? s2 : s2 ^ 1;
    }
}
