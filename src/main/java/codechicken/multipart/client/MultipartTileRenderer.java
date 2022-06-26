package codechicken.multipart.client;

import codechicken.lib.render.CCRenderState;
import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.render.PartRenderer;
import codechicken.multipart.block.TileMultiPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 31/8/20.
 */
public class MultipartTileRenderer implements BlockEntityRenderer<BlockEntity> {

    public MultipartTileRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(BlockEntity t, float partialTicks, PoseStack mStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!(t instanceof TileMultiPart tile)) return;
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;
        for (TMultiPart p : tile.getPartList()) {
            PartRenderer<?> renderer = MultipartClientRegistry.getRenderer(p.getType());
            if (renderer != null) {
                renderer.renderDynamic(unsafeCast(p), mStack, buffers, packedLight, packedOverlay, partialTicks);
            }
        }
    }
}
