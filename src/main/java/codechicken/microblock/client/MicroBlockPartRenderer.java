package codechicken.microblock.client;

import codechicken.lib.render.CCRenderState;
import codechicken.microblock.api.MicroMaterialClient;
import codechicken.microblock.part.MicroblockPart;
import codechicken.multipart.api.part.render.PartRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 20/10/22.
 */
public class MicroBlockPartRenderer implements PartRenderer<MicroblockPart> {

    public static final MicroBlockPartRenderer INSTANCE = new MicroBlockPartRenderer();

    @Override
    public void renderStatic(MicroblockPart part, @Nullable RenderType layer, CCRenderState ccrs) {
        MicroMaterialClient clientMaterial = MicroMaterialClient.get(part.material);
        if (clientMaterial != null) {
            clientMaterial.renderCuboids(ccrs, layer, part.getRenderCuboids(false));
        }
    }

    @Override
    public void renderDynamic(MicroblockPart part, PoseStack pStack, MultiBufferSource buffers, int packedLight, int packedOverlay, float partialTicks) {
        MicroMaterialClient clientMaterial = MicroMaterialClient.get(part.material);
        if (clientMaterial != null) {
            clientMaterial.renderDynamic(part, null, pStack, buffers, packedLight, packedOverlay, partialTicks);
        }
    }
}
