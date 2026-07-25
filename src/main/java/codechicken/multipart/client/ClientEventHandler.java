package codechicken.multipart.client;

import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.multipart.api.part.render.OutlinePartRenderer;
import codechicken.multipart.block.BlockMultipart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 2/9/20.
 */
public class ClientEventHandler {

    public static void init() {
        NeoForge.EVENT_BUS.addListener(ClientEventHandler::onDrawBlockHighlight);
    }

    private static void onDrawBlockHighlight(ExtractBlockOutlineRenderStateEvent event) {
        if (!(event.getHitResult() instanceof PartRayTraceResult hit)) return;

        // Let's just make sure we actually hit the part, unsure why this would never be the case, but /shrug
        TileMultipart tile = BlockMultipart.getTile(event.getLevel(), hit.getBlockPos());
        if (tile == null) return;

        var renderer = MultipartClientRegistry.getOutlinePartRenderer(hit.part.getType());
        if (renderer != null) {
            boolean[] addedCustomRenderer = { false };
            boolean rendererHandled = renderer.extractPartOutline(
                    unsafeCast(hit.part),
                    hit,
                    event.getLevelRenderer(),
                    event.getLevelRenderState(),
                    event.getCamera(),
                    event.getCollisionContext(),
                    event.isInTranslucentPass(),
                    event.isHighContrast(),
                    (e) -> {
                        addedCustomRenderer[0] = true;
                        event.addCustomRenderer(e);
                    }
            );
            if (rendererHandled) {
                // The part renderer may have set LevelRenderState.blockOutlineRenderState, the same as us
                // or added to the events `CustomBlockOutlineRenderer` list.
                // If they did the former, we need to cancel the event to prevent vanilla overwriting it,
                // if they added a CustomBlockOutlineRenderer, we need to _not_ cancel the event..
                if (!addedCustomRenderer[0]) {
                    event.setCanceled(true);
                }
                return;
            }
        }

        event.getLevelRenderState().blockOutlineRenderState = new BlockOutlineRenderState(
                event.getBlockPos(),
                event.isInTranslucentPass(),
                event.isHighContrast(),
                hit.hitShape,
                List.of()
        );
        event.setCanceled(true);
    }
}
