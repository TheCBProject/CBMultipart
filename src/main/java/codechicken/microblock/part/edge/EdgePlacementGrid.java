package codechicken.microblock.part.edge;

import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.part.PlacementGrid;
import codechicken.multipart.util.PartMap;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Created by covers1624 on 21/10/22.
 */
public class EdgePlacementGrid extends PlacementGrid {

    public static final EdgePlacementGrid EDGE_GRID = new EdgePlacementGrid();

    @Override
    protected void bufferLines(VertexConsumer cons, Vector3 norm) {
        cons.vertex(-0.5, 0, -0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(-0.5, 0, 0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(-0.5, 0, 0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(0.5, 0, 0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(0.5, 0, 0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(0.5, 0, -0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(0.5, 0, -0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(-0.5, 0, -0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(0.25, 0, -0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(0.25, 0, 0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(-0.25, 0, -0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(-0.25, 0, 0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(-0.5, 0, 0.25).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(0.5, 0, 0.25).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(-0.5, 0, -0.25).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(0.5, 0, -0.25).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
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
