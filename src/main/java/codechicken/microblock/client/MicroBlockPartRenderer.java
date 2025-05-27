package codechicken.microblock.client;

import codechicken.lib.render.CCRenderState;
import codechicken.microblock.api.MicroMaterialClient;
import codechicken.microblock.part.MicroblockPart;
import codechicken.multipart.api.part.render.PartRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by covers1624 on 20/10/22.
 */
public class MicroBlockPartRenderer implements PartRenderer<MicroblockPart> {

    public static final MicroBlockPartRenderer INSTANCE = new MicroBlockPartRenderer();

    @Override
    public List<BakedQuad> getQuads(MicroblockPart part, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        MicroMaterialClient clientMaterial = MicroMaterialClient.get(part.material);
        if (clientMaterial == null) return List.of();

        return clientMaterial.getQuads(part, side, renderType, part.getRenderCuboids(false));
    }

    @Override
    @Deprecated
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
