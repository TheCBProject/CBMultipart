package codechicken.multipart.api.part.render;

import codechicken.lib.render.CCRenderState;
import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.util.PartRayTraceResult;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.DrawSelectionEvent;

import javax.annotation.Nullable;

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
     * @return If any vertices were drawn.
     */
    default boolean renderStatic(T part, @Nullable RenderType layer, CCRenderState ccrs) {
        return false;
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
     */
    default void renderBreaking(T part, CCRenderState ccrs) {
        renderStatic(part, null, ccrs);
    }

    /**
     * Render the dynamic, changing faces of this part and/or other glfx.
     *
     * @param part          The {@link MultiPart} being rendered.
     * @param mStack        The {@link PoseStack} to apply.
     * @param buffers       The {@link MultiBufferSource} storage.
     * @param packedLight   The packed LightMap coords to use. See {@link LightTexture}.
     * @param packedOverlay The packed Overlay coords to use. See {@link OverlayTexture}.
     * @param partialTicks  The game partial ticks.
     */
    default void renderDynamic(T part, PoseStack mStack, MultiBufferSource buffers, int packedLight, int packedOverlay, float partialTicks) { }

    /**
     * Override the drawing of the selection box around this part.
     * <p>
     * This is called with the context of {@link DrawSelectionEvent.HighlightBlock}.
     *
     * @param part         The {@link MultiPart} being rendered.
     * @param hit          The {@link PartRayTraceResult}.
     * @param info         The {@link Camera} camera info.
     * @param mStack       The {@link PoseStack} to apply.
     * @param buffers      The {@link MultiBufferSource} storage.
     * @param partialTicks The game partial ticks.
     * @return If any custom rendering was applied. <code>false</code> for default {@link VoxelShape} based rendering.
     */
    default boolean drawHighlight(T part, PartRayTraceResult hit, Camera info, PoseStack mStack, MultiBufferSource buffers, float partialTicks) {
        return false;
    }
}
