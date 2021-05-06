package codechicken.multipart.client;

import codechicken.lib.render.CCRenderState;
import codechicken.multipart.block.TileMultiPart;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by covers1624 on 31/8/20.
 */
public class MultipartTileRenderer extends TileEntityRenderer<TileEntity> {

    public MultipartTileRenderer(TileEntityRendererDispatcher rendererDispatcher) {
        super(rendererDispatcher);
    }

    @Override
    public void render(TileEntity t, float partialTicks, MatrixStack mStack, IRenderTypeBuffer buffers, int packedLight, int packedOverlay) {
        if (!(t instanceof TileMultiPart)) return;
        TileMultiPart tile = (TileMultiPart) t;
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;
        tile.getPartList().forEach(p -> p.renderDynamic(mStack, buffers, packedLight, packedOverlay, partialTicks));
    }
}
