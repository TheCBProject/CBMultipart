package codechicken.multipart.api.part.render;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.buffer.BakedQuadVertexBuilder;
import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.util.PartRayTraceResult;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Responsible for all rendering related operations of a {@link MultiPart}.
 * <p>
 * Registered via {@link MultipartClientRegistry#register}.
 * <p>
 * Created by covers1624 on 7/11/21.
 *
 * @see PartBakedModelRenderer
 */
public interface PartRenderer<T extends MultiPart> {

    /**
     * Get the static quads for this part, this is synonymous to {@link BakedModel#getQuads(BlockState, Direction, RandomSource, ModelData, RenderType)}
     * <p>
     * This is method may be called on the chunk batching thread. World/state access should be performed in a thread-safe manner.
     * <p>
     * It is highly recommended that parts do some form of caching for the data returned here.
     * <p>
     * The current default implementation of this method delegates to {@link #renderStatic} and {@link #renderBreaking}. When these methods
     * are removed, this method will turn into a no-op.
     *
     * @param part       The part quads are required for.
     * @param side       The side of the model requesting quads. Non-null sides will be culled if they are occluded. {@code null} for un-culled.
     * @param rand       The {@link RandomSource} for this block position.
     * @param data       Any {@link ModelData} this part has provided via {@link MultiPart#getModelData()}.
     * @param renderType The {@link RenderType} pass. {@code null} is used for breaking overlay and other special rendering (enderman, etc).
     *                   When {@code null}, if the player is currently looking at a part, only that part will be queried for quads.
     * @return The quads, or an empty list.
     */
    default List<BakedQuad> getQuads(T part, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        // We can't face cull here.
        if (side != null) return List.of();

        CCRenderState ccrs = CCRenderState.instance();
        ccrs.hackyReallyDontComputeLighting = true;
        ccrs.reset();
        BakedQuadVertexBuilder builder = new BakedQuadVertexBuilder();
        ccrs.bind(builder, DefaultVertexFormat.BLOCK);
        ccrs.lightMatrix.locate(part.level(), part.pos());
        if (renderType == null) {
            renderBreaking(part, ccrs);
        } else {
            renderStatic(part, renderType, ccrs);
        }
        ccrs.hackyReallyDontComputeLighting = false;
        ccrs.reset();
        return builder.bake();
    }

    /**
     * Render the static, unmoving faces of this part into the world renderer.
     * <p>
     * The given CCRenderState is set up as follows should you wish to use it:
     * <pre>
     * - {@link CCRenderState#reset()} has been called.
     * - The current buffer is bound to {@link CCRenderState}.
     * - The {@link CCRenderState#lightMatrix LightMatrix} is setup and ready to use.
     * </pre>
     * <p>
     * Should you wish to not use {@link CCRenderState} and associated utilities. You can obtain
     * the raw {@link VertexConsumer} from {@link CCRenderState#getConsumer()} and the {@link VertexFormat}
     * from {@link CCRenderState#getVertexFormat()}.
     * <p>
     * If you wish to render your part as a standard {@link BakedModel} please see {@link PartBakedModelRenderer}.
     * <p>
     * This method may be called on chunk batching threads, all operations performed here must be thread aware.
     * <p>
     * It is illegal to perform raw GL calls within this method. You will not have a valid GL context, or, a context from another thread.
     *
     * @param part  The {@link MultiPart} being rendered.
     * @param layer The block {@link RenderType} layer being rendered. <code>null</code> for {@link #renderBreaking}
     * @param ccrs  The {@link CCRenderState} instance to render with.
     * @deprecated Raw chunk buffer access is being phased out, this is known to be incompatible with many mods. Parts should
     * migrate to returning {@link BakedQuad}s from {@link #getQuads}.
     */
    @Deprecated
    default void renderStatic(T part, @Nullable RenderType layer, CCRenderState ccrs) {
    }

    /**
     * Override how your part displays its breaking progress overlay.
     * <p>
     * By default, this method will delegate to {@link #renderStatic(MultiPart, RenderType, CCRenderState)}
     * using a <code>null</code> {@link RenderType}.
     * <p>
     * You shouldn't need to override this, in most cases the defaults will work just fine.
     * <p>
     * The given CCRenderState is set up as follows should you wish to use it:
     * <pre>
     * - {@link CCRenderState#reset()} has been called.
     * - The current buffer is bound to {@link CCRenderState}.
     * - The {@link CCRenderState#lightMatrix LightMatrix} is setup and ready to use.
     * </pre>
     * <p>
     * Should you wish to not use {@link CCRenderState} and associated utilities. You can obtain
     * the raw {@link VertexConsumer} from {@link CCRenderState#getConsumer()} and the {@link VertexFormat}
     * from {@link CCRenderState#getVertexFormat()}.
     * <p>
     * This method may be called on chunk batching threads, all operations performed here must be thread aware.
     * <p>
     * It is illegal to perform raw GL calls within this method. You will not have a valid GL context, or, a context from another thread.
     *
     * @param part The {@link MultiPart} being rendered.
     * @param ccrs The {@link CCRenderState} instance to render with.
     * @deprecated Raw chunk buffer access is being phased out, this is known to be incompatible with many mods. Parts should
     * migrate to returning {@link BakedQuad}s from {@link #getQuads}, using the {@code null} render type as a marker for breaking.
     */
    @Deprecated
    default void renderBreaking(T part, CCRenderState ccrs) {
        renderStatic(part, null, ccrs);
    }

    /**
     * Render the dynamic, changing faces of this part and/or other glfx.
     *
     * @param part          The {@link MultiPart} being rendered.
     * @param pStack        The {@link PoseStack} to apply.
     * @param buffers       The {@link MultiBufferSource} storage.
     * @param packedLight   The packed LightMap coords to use. See {@link LightTexture}.
     * @param packedOverlay The packed Overlay coords to use. See {@link OverlayTexture}.
     * @param partialTicks  The game partial ticks.
     */
    default void renderDynamic(T part, PoseStack pStack, MultiBufferSource buffers, int packedLight, int packedOverlay, float partialTicks) { }

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
    default boolean drawHighlight(T part, PartRayTraceResult hit, Camera camera, PoseStack pStack, MultiBufferSource buffers, float partialTicks) {
        return false;
    }
}
