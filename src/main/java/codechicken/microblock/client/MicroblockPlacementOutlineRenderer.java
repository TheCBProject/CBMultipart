package codechicken.microblock.client;

import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.lib.vec.Line3;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.util.Sides;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.client.RenderTypeHelper;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Created by covers1624 on 7/20/26.
 */
public class MicroblockPlacementOutlineRenderer implements CustomBlockOutlineRenderer {

    private final Vector3 hit;
    private final int side;
    private final List<Line3> lines;
    private final @Nullable PlacementPreview preview;

    public MicroblockPlacementOutlineRenderer(Vector3 hit, int side, List<Line3> lines, @Nullable PlacementPreview preview) {
        this.hit = hit;
        this.side = side;
        this.lines = lines;
        this.preview = preview;
    }

    @Override
    public boolean render(BlockOutlineRenderState renderState, MultiBufferSource.BufferSource buffers, PoseStack poseStack, boolean translucentPass, LevelRenderState levelRenderState) {
        if (renderState.isTranslucent() != translucentPass) return true;

        var camera = levelRenderState.cameraRenderState.pos;
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        renderPlacementGrid(poseStack.last(), buffers);
        renderPlacementPreview(poseStack, buffers);
        poseStack.popPose();

        return true;
    }

    private void renderPlacementGrid(PoseStack.Pose pose, MultiBufferSource.BufferSource buffers) {
        Matrix4 mat = new Matrix4(pose);
        transformFace(hit, side, mat);
        VertexConsumer cons = new TransformingVertexConsumer(buffers.getBuffer(RenderTypes.lines()), mat);
        // TODO high contrast?
        var lineWidth = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
        for (Line3 line : lines) {
            bufferLinePair(cons, line.pt1, line.pt2, 0F, 0F, 0F, 0.4f, lineWidth);
        }
        // Explicit end batch so transparency with the placement preview works.
        buffers.endBatch(RenderTypes.lines());
    }

    private void bufferLinePair(VertexConsumer builder, Vector3 v1, Vector3 v2, float r, float g, float b, float a, float lineWidth) {
        Vector3 vn = v1.copy().subtract(v2);
        double d = vn.mag();
        vn.divide(d);
        builder.addVertex((float) v1.x, (float) v1.y, (float) v1.z).setColor(r, g, b, a).setNormal((float) vn.x, (float) vn.y, (float) vn.z).setLineWidth(lineWidth);
        builder.addVertex((float) v2.x, (float) v2.y, (float) v2.z).setColor(r, g, b, a).setNormal((float) vn.x, (float) vn.y, (float) vn.z).setLineWidth(lineWidth);
    }

    private void transformFace(Vector3 hit, int side, Matrix4 mat) {
        Vector3 pos = hit.copy().floor().add(Vector3.CENTER);
        mat.translate(pos);
        mat.apply(Rotation.sideRotations[side]);
        Vector3 rHit = pos.copy().subtract(hit).apply(Rotation.sideRotations[side ^ 1].inverse());
        mat.translate(0, rHit.y - 0.002, 0);
    }

    private void renderPlacementPreview(PoseStack poseStack, MultiBufferSource.BufferSource buffers) {
        if (preview == null) return;

        poseStack.pushPose();
        poseStack.translate(preview.pos.getX(), preview.pos.getY(), preview.pos.getZ());

        var renderType = RenderTypeHelper.getEntityRenderType(ChunkSectionLayer.TRANSLUCENT);
        renderPreviewParts(poseStack.last(), buffers.getBuffer(renderType), preview.parts);
        buffers.endBatch(renderType);

        poseStack.popPose();
    }

    private static void renderPreviewParts(PoseStack.Pose pose, VertexConsumer consumer, List<BlockModelPart> parts) {
        for (var modelPart : parts) {
            for (var side : Sides.SIDES_AND_NULL) {
                for (BakedQuad quad : modelPart.getQuads(side)) {
                    consumer.putBulkData(pose, quad, 1.0F, 1.0F, 1.0F, 0.4f, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
                }
            }
        }
    }

    public record PlacementPreview(BlockPos pos, List<BlockModelPart> parts) { }
}
