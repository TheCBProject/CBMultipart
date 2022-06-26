package codechicken.multipart.client;

import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.Matrix4;
import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.render.PartRenderer;
import codechicken.multipart.block.BlockMultiPart;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.util.PartRayTraceResult;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.common.MinecraftForge;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 2/9/20.
 */
public class ClientEventHandler {

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::onDrawBlockHighlight);
    }

    private static void onDrawBlockHighlight(DrawSelectionEvent.HighlightBlock event) {
        Camera camera = event.getCamera();
        PoseStack mStack = event.getPoseStack();
        MultiBufferSource buffers = event.getMultiBufferSource();
        float partialTicks = event.getPartialTicks();
        BlockHitResult target = event.getTarget();
        if (!(target instanceof PartRayTraceResult hit)) return;

        TileMultiPart tile = BlockMultiPart.getTile(camera.getEntity().level, target.getBlockPos());
        if (tile == null) return;

        TMultiPart part = hit.part;

        PartRenderer<?> renderer = MultipartClientRegistry.getRenderer(part.getType());

        if (renderer != null && !renderer.drawHighlight(unsafeCast(part), hit, camera, mStack, buffers, partialTicks)) {
            Matrix4 mat = new Matrix4(mStack);
            mat.translate(hit.getBlockPos());
            RenderUtils.bufferShapeHitBox(mat, buffers, camera, part.getShape(CollisionContext.empty()));
        }
        event.setCanceled(true);
    }
}
