package codechicken.multipart.api.part.render;

import codechicken.multipart.api.RegisterMultipartRenderersEvent;
import codechicken.multipart.api.part.MultiPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Created by covers1624 on 7/26/26.
 *
 * @see RegisterMultipartRenderersEvent
 */
public interface DynamicPartRenderer<P extends MultiPart, S> {

    /**
     * Create the state instance used for dynamic {@link BlockEntityRenderer} based rendering.
     *
     * @return The state. {@code null} will disable dynamic rendering.
     */
    @Nullable S createDynamicState();

    /**
     * Extract any state required to render this part.
     * <p>
     * You should avoid binding your Part instance directly into the state, in the future
     * extract and submit may not always run on the same thread.
     *
     * @param beState       Useful information from the BE's own state.
     * @param state         Your state to fill.
     * @param part          Your part.
     * @param partialTick   The current partial ticks.
     * @param camera        The camera location.
     * @param breakProgress The breaking progress.
     */
    void extractDynamicState(BEState beState, S state, P part, float partialTick, Vec3 camera, @Nullable CrumblingOverlay breakProgress);

    /**
     * Submit your dynamic geometry.
     *
     * @param beState     Useful information from the BE's own state.
     * @param state       Your state with filled data.
     * @param poseStack   The current pose stack.
     * @param collector   The submission collector.
     * @param cameraState The camera state.
     */
    void submitDynamic(BEState beState, S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState);

    /**
     * Contains properties from the base {@link BlockEntityRenderState} that are relevant to parts.
     *
     * @param pos           The block position.
     * @param packedLight   The packed lightmap.
     * @param breakProgress The break progress.
     */
    record BEState(BlockPos pos, int packedLight, @Nullable CrumblingOverlay breakProgress) { }

    @FunctionalInterface
    interface Factory<P extends MultiPart, S> {

        DynamicPartRenderer<P, S> create();
    }
}
