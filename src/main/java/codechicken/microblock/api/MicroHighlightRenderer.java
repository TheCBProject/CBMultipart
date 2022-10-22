package codechicken.microblock.api;

import codechicken.microblock.part.StandardMicroFactory;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Used to override the default micro material placement highlight rendering.
 * <p>
 * Created by covers1624 on 22/10/22.
 */
public interface MicroHighlightRenderer {

    /**
     * Called to handle any custom highlight rendering.
     *
     * @return {@code true} If default rendering should be skipped.
     */
    boolean renderHighlight(Player player, InteractionHand hand, BlockHitResult hit, StandardMicroFactory factory, int size, MicroMaterial material, PoseStack pStack, MultiBufferSource buffers, float partialTicks);
}
