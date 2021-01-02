package codechicken.multipart.client;

import codechicken.lib.render.CCRenderState;
import codechicken.multipart.block.TileMultiPart;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

/**
 * Created by covers1624 on 31/8/20.
 */
public class MultipartTileRenderer extends TileEntityRenderer<TileMultiPart> {

    public MultipartTileRenderer(TileEntityRendererDispatcher rendererDispatcher) {
        super(rendererDispatcher);
    }

    @Override
    public void render(TileMultiPart tile, float partialTicks, MatrixStack mStack, IRenderTypeBuffer buffers, int packedLight, int packedOverlay) {
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;
        tile.getPartList().forEach(p -> p.renderDynamic(mStack, buffers, packedLight, packedOverlay, partialTicks));
    }
}
