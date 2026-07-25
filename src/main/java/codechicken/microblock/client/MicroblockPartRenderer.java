package codechicken.microblock.client;

import codechicken.microblock.api.MicroMaterialClient;
import codechicken.microblock.part.MicroblockPart;
import codechicken.multipart.api.part.render.StaticPartRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.List;

/**
 * Created by covers1624 on 20/10/22.
 */
public class MicroblockPartRenderer implements StaticPartRenderer<MicroblockPart> {

    @Override
    public void collectParts(ModelData modelData, BlockAndTintGetter level, BlockPos pos, RandomSource rand, List<BlockModelPart> parts) {
        var microblockData = modelData.get(MicroblockModelData.TYPE);
        if (microblockData == null) return;

        var clientMaterial = MicroMaterialClient.get(microblockData.material());
        if (clientMaterial == null) return;

        clientMaterial.collectParts(level, pos, microblockData.renderCuboids(), parts);
    }

    // TODO this needs a whole rethink.
//    @Override
//    public void renderDynamic(MicroblockPart part, PoseStack pStack, MultiBufferSource buffers, int packedLight, int packedOverlay, float partialTicks) {
//        MicroMaterialClient clientMaterial = MicroMaterialClient.get(part.material);
//        if (clientMaterial != null) {
//            clientMaterial.renderDynamic(part, null, pStack, buffers, packedLight, packedOverlay, partialTicks);
//        }
//    }
}
