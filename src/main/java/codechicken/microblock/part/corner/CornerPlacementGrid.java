package codechicken.microblock.part.corner;

import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.part.PlacementGrid;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Created by covers1624 on 21/10/22.
 */
public class CornerPlacementGrid extends PlacementGrid {

    public static final CornerPlacementGrid CORNER_GRID = new CornerPlacementGrid();

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

        cons.vertex(0, 0, -0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(0, 0, 0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();

        cons.vertex(-0.5, 0, 0).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(0.5, 0, 0).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
    }

    @Override
    public int getHitSlot(Vector3 vHit, int side) {
        int s1 = ((side & 6) + 3) % 6;
        int s2 = ((side & 6) + 5) % 6;
        double u = vHit.copy().add(-0.5, -0.5, -0.5).scalarProject(Rotation.axes[s1]);
        double v = vHit.copy().add(-0.5, -0.5, -0.5).scalarProject(Rotation.axes[s2]);

        int bu = u >= 0 ? 1 : 0;
        int bv = v >= 0 ? 1 : 0;
        int bw = (side & 1) ^ 1;

        return 7 + (bw << (side >> 1) | bu << (s1 >> 1) | bv << (s2 >> 1));
    }
}
