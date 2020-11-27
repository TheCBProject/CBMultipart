package codechicken.multipart.client;

import codechicken.lib.render.CCRenderState;
import codechicken.multipart.TileMultipart;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

/**
 * Created by covers1624 on 31/8/20.
 */
public class MultipartTileRenderer extends TileEntityRenderer<TileMultipart> {

    public MultipartTileRenderer(TileEntityRendererDispatcher rendererDispatcher) {
        super(rendererDispatcher);
    }

    @Override
    public void render(TileMultipart tile, float partialTicks, MatrixStack mStack, IRenderTypeBuffer buffers, int packedLight, int packedOverlay) {
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;
        tile.jPartList().forEach(p -> p.renderDynamic(mStack, buffers, packedLight, packedOverlay, partialTicks));
    }
}
