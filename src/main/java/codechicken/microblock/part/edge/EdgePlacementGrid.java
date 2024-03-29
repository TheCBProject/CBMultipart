package codechicken.microblock.part.edge;

import codechicken.lib.vec.Line3;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.part.PlacementGrid;
import codechicken.multipart.util.PartMap;
import net.covers1624.quack.util.LazyValue;

import java.util.List;

/**
 * Created by covers1624 on 21/10/22.
 */
public class EdgePlacementGrid extends PlacementGrid {

    public static final EdgePlacementGrid EDGE_GRID = new EdgePlacementGrid();
    private static final LazyValue<List<Line3>> LINES = new LazyValue<>(() -> List.of(
            new Line3(-0.5, 0, -0.5, -0.5, 0, 0.5),
            new Line3(-0.5, 0, 0.5, 0.5, 0, 0.5),
            new Line3(0.5, 0, 0.5, 0.5, 0, -0.5),
            new Line3(0.5, 0, -0.5, -0.5, 0, -0.5),
            new Line3(0.25, 0, -0.5, 0.25, 0, 0.5),
            new Line3(-0.25, 0, -0.5, -0.25, 0, 0.5),
            new Line3(-0.5, 0, 0.25, 0.5, 0, 0.25),
            new Line3(-0.5, 0, -0.25, 0.5, 0, -0.25)
    ));

    @Override
    public List<Line3> getOverlayLines() {
        return LINES.get();
    }

    @Override
    public int getHitSlot(Vector3 vHit, int side) {
        int s1 = (side + 2) % 6;
        int s2 = (side + 4) % 6;
        double u = vHit.copy().add(-0.5, -0.5, -0.5).scalarProject(Rotation.axes[s1]);
        double v = vHit.copy().add(-0.5, -0.5, -0.5).scalarProject(Rotation.axes[s2]);

        if (Math.abs(u) < 4 / 16D && Math.abs(v) < 4 / 16D) {
            return -1;
        }

        if (Math.abs(u) > 4 / 16D && Math.abs(v) > 4 / 16D) {
            return PartMap.edgeBetween(u > 0 ? s1 : s1 ^ 1, v > 0 ? s2 : s2 ^ 1);
        }

        int s = Math.abs(u) > Math.abs(v) ? (u > 0 ? s1 : s1 ^ 1) : (v > 0 ? s2 : s2 ^ 1);

        return PartMap.edgeBetween(side ^ 1, s);
    }
}
