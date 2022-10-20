package codechicken.microblock.part.face;

import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.part.PlacementGrid;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Created by covers1624 on 20/10/22.
 */
public class FaceEdgeGrid extends PlacementGrid {

    public static final FaceEdgeGrid FACE_PLACEMENT_GRID = new FaceEdgeGrid(1 / 4D);
    public static final FaceEdgeGrid HOLLOW_PLACEMENT_GRID = new FaceEdgeGrid(3 / 8D);

    private final double size;

    public FaceEdgeGrid(double size) {
        this.size = size;
    }

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

        cons.vertex(0.5, 0, 0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(size, 0, size).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();

        cons.vertex(-0.5, 0, 0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(-size, 0, size).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();

        cons.vertex(0.5, 0, -0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(size, 0, -size).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();

        cons.vertex(-0.5, 0, -0.5).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(-size, 0, -size).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();

        cons.vertex(-size, 0, -size).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(-size, 0, size).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();

        cons.vertex(-size, 0, size).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(size, 0, size).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();

        cons.vertex(size, 0, size).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(size, 0, -size).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();

        cons.vertex(size, 0, -size).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
        cons.vertex(-size, 0, -size).color(0f, 0f, 0f, 1f).normal((float) norm.x, (float) norm.y, (float) norm.z).endVertex();
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
