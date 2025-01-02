package codechicken.multipart.client;

import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.Matrix4;
import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.render.PartRenderer;
import codechicken.multipart.block.BlockMultipart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.util.PartRayTraceResult;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.common.NeoForge;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 2/9/20.
 */
public class ClientEventHandler {

    public static void init() {
        NeoForge.EVENT_BUS.addListener(ClientEventHandler::onDrawBlockHighlight);
    }

    private static void onDrawBlockHighlight(RenderHighlightEvent.Block event) {
        Camera camera = event.getCamera();
        PoseStack mStack = event.getPoseStack();
        MultiBufferSource buffers = event.getMultiBufferSource();
        float partialTicks = event.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        BlockHitResult target = event.getTarget();
        if (!(target instanceof PartRayTraceResult hit)) return;

        TileMultipart tile = BlockMultipart.getTile(camera.getEntity().level(), target.getBlockPos());
        if (tile == null) return;

        MultiPart part = hit.part;

        PartRenderer<?> renderer = MultipartClientRegistry.getRenderer(part.getType());

        if (renderer == null || !renderer.drawHighlight(unsafeCast(part), hit, camera, mStack, buffers, partialTicks)) {
            Matrix4 mat = new Matrix4(mStack);
            mat.translate(hit.getBlockPos());
            RenderUtils.bufferShapeHitBox(mat, buffers, camera, part.getShape(CollisionContext.empty()));
        }
        event.setCanceled(true);
    }
}
