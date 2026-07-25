package codechicken.multipart.api.part.render;

import codechicken.multipart.api.RegisterMultipartRenderersEvent;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;

import java.util.function.Consumer;

/**
 * Created by covers1624 on 7/26/26.
 *
 * @see RegisterMultipartRenderersEvent
 */
public interface OutlinePartRenderer<T extends MultiPart> {

    /**
     * Override the drawing of the selection box around this part.
     * <p>
     * This is called with the context of {@link ExtractBlockOutlineRenderStateEvent}.
     *
     * @param part             The {@link MultiPart} being rendered.
     * @param hit              The {@link PartRayTraceResult}.
     * @param levelRenderer    The {@link LevelRenderer}.
     * @param levelRenderState The {@link LevelRenderState}.
     * @param camera           The {@link Camera} camera info.
     * @param collisionCtx     The {@link CollisionContext}.
     * @param cons             Consumer to add {@link CustomBlockOutlineRenderer}s.
     * @return If any custom rendering was applied. <code>false</code> for default {@link VoxelShape} based rendering.
     */
    boolean extractPartOutline(T part, PartRayTraceResult hit, LevelRenderer levelRenderer, LevelRenderState levelRenderState, Camera camera, CollisionContext collisionCtx, boolean isInTranslucentPass, boolean isHighContrast, Consumer<CustomBlockOutlineRenderer> cons);
}
