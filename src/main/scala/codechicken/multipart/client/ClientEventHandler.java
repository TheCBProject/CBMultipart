package codechicken.multipart.client;

import codechicken.multipart.BlockMultipart;
import codechicken.multipart.TileMultipartClient;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.BlockRayTraceResult;
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
        BlockRayTraceResult target = event.getTarget();

        if (target instanceof PartRayTraceResult) {
            PartRayTraceResult hit = (PartRayTraceResult) target;
            TileMultipartClient tile = BlockMultipart.getClientTile(info.getRenderViewEntity().world, target.getPos());
            if (tile != null) {
                tile.drawHighlight(hit, info, event.getMatrix(), event.getBuffers(), event.getPartialTicks());
                event.setCanceled(true);
            }
        }
    }
}
