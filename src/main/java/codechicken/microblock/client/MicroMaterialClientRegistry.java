package codechicken.microblock.client;

import codechicken.microblock.api.MicroHighlightRenderer;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.part.StandardMicroFactory;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by covers1624 on 22/10/22.
 */
public class MicroMaterialClientRegistry {

    private final static Map<MicroMaterial, List<MicroHighlightRenderer>> SPECIFIC_HIGHLIGHT_RENDERERS = new HashMap<>();
    private final static List<MicroHighlightRenderer> GLOBAL_HIGHLIGHT_RENDERERS = new LinkedList<>();

    public static void registerHighlightRenderer(MicroMaterial material, MicroHighlightRenderer renderer) {
        synchronized (SPECIFIC_HIGHLIGHT_RENDERERS) {
            SPECIFIC_HIGHLIGHT_RENDERERS.computeIfAbsent(material, e -> new LinkedList<>()).add(renderer);
        }
    }

    public static void registerGlobalHighlightRenderer(MicroHighlightRenderer renderer) {
        synchronized (GLOBAL_HIGHLIGHT_RENDERERS) {
            GLOBAL_HIGHLIGHT_RENDERERS.add(renderer);
        }
    }

    public static boolean renderHighlightOverride(Player player, InteractionHand hand, BlockHitResult hit, StandardMicroFactory factory, int size, MicroMaterial material, PoseStack pStack, MultiBufferSource buffers, float partialTicks) {
        List<MicroHighlightRenderer> specific = SPECIFIC_HIGHLIGHT_RENDERERS.getOrDefault(material, List.of());
        for (MicroHighlightRenderer renderer : specific) {
            if (renderer.renderHighlight(player, hand, hit, factory, size, material, pStack, buffers, partialTicks)) {
                return true;
            }
        }
        for (MicroHighlightRenderer renderer : GLOBAL_HIGHLIGHT_RENDERERS) {
            if (renderer.renderHighlight(player, hand, hit, factory, size, material, pStack, buffers, partialTicks)) {
                return true;
            }
        }
        return false;
    }
}
