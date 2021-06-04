package codechicken.multipart.client;

import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.Matrix4;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.BlockMultiPart;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.util.PartRayTraceResult;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 * Created by covers1624 on 2/9/20.
 */
public class ClientEventHandler {

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::onDrawBlockHighlight);
    }

    private static void onDrawBlockHighlight(DrawHighlightEvent.HighlightBlock event) {
        ActiveRenderInfo info = event.getInfo();
        MatrixStack mStack = event.getMatrix();
        IRenderTypeBuffer buffers = event.getBuffers();
        float partialTicks = event.getPartialTicks();
        BlockRayTraceResult target = event.getTarget();
        if (!(target instanceof PartRayTraceResult)) return;

        PartRayTraceResult hit = (PartRayTraceResult) target;
        TileMultiPart tile = BlockMultiPart.getTile(info.getEntity().level, target.getBlockPos());
        if (tile == null) return;

        TMultiPart part = hit.part;

        if (!part.drawHighlight(hit, info, mStack, buffers, partialTicks)) {
            Matrix4 mat = new Matrix4(mStack);
            mat.translate(hit.getBlockPos());
            RenderUtils.bufferShapeHitBox(mat, buffers, info, part.getShape(ISelectionContext.empty()));
        }
        event.setCanceled(true);
    }
}
