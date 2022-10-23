package codechicken.microblock.client;

import codechicken.lib.render.CCRenderState;
import codechicken.microblock.api.BlockMicroMaterial;
import codechicken.microblock.api.MicroMaterialClient;
import codechicken.microblock.part.MicroblockPart;
import codechicken.multipart.api.part.render.PartRenderer;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 20/10/22.
 */
public class MicroBlockPartRenderer implements PartRenderer<MicroblockPart> {

    public static final MicroBlockPartRenderer INSTANCE = new MicroBlockPartRenderer();

    @Override
    public boolean renderStatic(MicroblockPart part, @Nullable RenderType layer, CCRenderState ccrs) {
        MicroMaterialClient clientMaterial = MicroMaterialClient.get(part.material);
        if (clientMaterial != null) {
            return clientMaterial.renderCuboids(ccrs, layer, part.getRenderCuboids(false));
        }
        return false;
    }
}
