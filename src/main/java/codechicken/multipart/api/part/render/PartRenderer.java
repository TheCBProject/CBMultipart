package codechicken.multipart.api.part.render;

import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.util.PartRayTraceResult;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.model.data.ModelData;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;

/**
 * Responsible for all rendering related operations of a {@link MultiPart}.
 * <p>
 * Registered via {@link MultipartClientRegistry#register}.
 * <p>
 * Created by covers1624 on 7/11/21.
 *
 * @see BlockStatePartBakedModelRenderer
 */
// TODO split this into 'StaticPartRenderer', `DynamicPartRenderer` and `HitboxPartRenderer`
//      With both static and dynamic being re-created dynamically, and event registered.
public interface PartRenderer<T extends MultiPart, S> {

    /**
     * Get the static quads for this part, this is synonymous to {@link BlockStateModel#collectParts(BlockAndTintGetter, BlockPos, BlockState, RandomSource, List)}
     * <p>
     * This is method may be called on the chunk batching thread. World/state access should be performed in a thread-safe manner.
     * <p>
     * It is highly recommended that parts do some form of caching for the data returned here.
     *
     * @param modelData The model data you returned from {@link MultiPart#getModelData()}.
     * @param level     The level.
     * @param pos       The block position.
     * @param rand      The {@link RandomSource} for this block position.
     * @param parts     The output collector.
     */
    default void collectParts(ModelData modelData, BlockAndTintGetter level, BlockPos pos, RandomSource rand, List<BlockModelPart> parts) { }

    /**
     * Create the state instance used for dynamic {@link BlockEntityRenderer} based rendering.
     *
     * @return The state. {@code null} will disable dynamic rendering.
     */
    default @Nullable S createDynamicState() {
        return null;
    }

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
    default void extractDynamicState(BEState beState, S state, T part, float partialTick, Vec3 camera, @Nullable CrumblingOverlay breakProgress) { }

    /**
     * Submit your dynamic geometry.
     *
     * @param beState     Useful information from the BE's own state.
     * @param state       Your state with filled data.
     * @param poseStack   The current pose stack.
     * @param collector   The submission collector.
     * @param cameraState The camera state.
     */
    default void submitDynamic(BEState beState, S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) { }

    /**
     * Override the drawing of the selection box around this part.
     * <p>
     * This is called with the context of {@link RenderHighlightEvent.Block}.
     *
     * @param part         The {@link MultiPart} being rendered.
     * @param hit          The {@link PartRayTraceResult}.
     * @param camera       The {@link Camera} camera info.
     * @param pStack       The {@link PoseStack} to apply.
     * @param buffers      The {@link MultiBufferSource} storage.
     * @param partialTicks The game partial ticks.
     * @return If any custom rendering was applied. <code>false</code> for default {@link VoxelShape} based rendering.
     */
    default boolean extractBlockHighlight(T part, PartRayTraceResult hit, LevelRenderer levelRenderer, LevelRenderState levelRenderState, Camera camera, CollisionContext collisionCtx, boolean isInTranslucentPass, boolean isHighContrast, Consumer<CustomBlockOutlineRenderer> cons) {
        return false;
    }

    record BEState(BlockPos pos, int lightCoords, @Nullable CrumblingOverlay breakProgress) { }
}
