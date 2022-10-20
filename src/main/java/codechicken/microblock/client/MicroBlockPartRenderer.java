package codechicken.microblock.client;

import codechicken.lib.render.CCRenderState;
import codechicken.microblock.api.BlockMicroMaterial;
import codechicken.microblock.part.MicroblockPart;
import codechicken.multipart.api.part.render.PartRenderer;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 20/10/22.
 */
public class MicroBlockPartRenderer implements PartRenderer<MicroblockPart> {

    @Override
    public boolean renderStatic(MicroblockPart part, @Nullable RenderType layer, CCRenderState ccrs) {
        if (layer == null || part.getMaterial().canRenderInLayer(layer)) {
            return MicroblockRender.renderCuboids(ccrs, ((BlockMicroMaterial) part.getMaterial()).state, layer, part.getRenderCuboids(false));
        }
        return PartRenderer.super.renderStatic(part, layer, ccrs);
    }
}
