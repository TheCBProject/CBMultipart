package codechicken.microblock.part;

import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.OptionalDouble;

/**
 * Created by covers1624 on 20/10/22.
 */
public abstract class PlacementGrid {

    private static final RenderType LINES = RenderType.create("lines", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, RenderType.CompositeState.builder()
            .setShaderState(RenderType.RENDERTYPE_LINES_SHADER)
            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(2)))
            .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
            .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
            .setOutputState(RenderType.ITEM_ENTITY_TARGET)
            .setWriteMaskState(RenderType.COLOR_DEPTH_WRITE)
            .setCullState(RenderType.NO_CULL)
            .createCompositeState(false)
    );

    public abstract int getHitSlot(Vector3 vHit, int side);

    @OnlyIn (Dist.CLIENT) // TODO, move rendering elsewhere..
    public void render(PoseStack pStack, Vector3 hit, int side, MultiBufferSource buffers) {
        Matrix4 mat = new Matrix4(pStack);
        transformFace(hit, side, mat);
        bufferLines(new TransformingVertexConsumer(buffers.getBuffer(LINES), mat), Rotation.axes[side]);
    }

    protected abstract void bufferLines(VertexConsumer cons, Vector3 norm);

    public void transformFace(Vector3 hit, int side, Matrix4 mat) {
        Vector3 pos = hit.copy().floor().add(Vector3.CENTER);
        mat.translate(pos);
        mat.apply(Rotation.sideRotations[side]);
        Vector3 rHit = pos.copy().subtract(hit).apply(Rotation.sideRotations[side ^ 1].inverse());
        mat.translate(0, rHit.y - 0.002, 0);
    }
}
