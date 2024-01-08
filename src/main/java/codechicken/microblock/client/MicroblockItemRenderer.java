package codechicken.microblock.client;

import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.api.MicroMaterialClient;
import codechicken.microblock.item.ItemMicroBlock;
import codechicken.microblock.part.MicroblockPart;
import codechicken.microblock.part.StandardMicroFactory;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Created by covers1624 on 20/10/22.
 */
public class MicroblockItemRenderer implements IItemRenderer {

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack mStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        MicroMaterial material = ItemMicroBlock.getMaterialFromStack(stack);
        StandardMicroFactory factory = ItemMicroBlock.getFactory(stack);
        int size = ItemMicroBlock.getSize(stack);

        if (material == null || factory == null) return;

        MicroMaterialClient clientMaterial = MicroMaterialClient.get(material);
        if (clientMaterial == null) return;

        MicroblockPart part = factory.create(true, material);
        part.setShape(size, factory.getItemSlot());

        mStack.pushPose();
        Vector3 offset = Vector3.CENTER.copy().subtract(part.getBounds().center());
        mStack.translate(offset.x, offset.y, offset.z);

        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;

        RenderType layer = clientMaterial.getItemRenderLayer();
        ccrs.bind(layer, buffers, mStack);
        clientMaterial.renderCuboids(ccrs, null, part.getRenderCuboids(true));

        clientMaterial.renderDynamic(part, transformType, mStack, buffers, packedLight, packedOverlay, 0);
        mStack.popPose();
    }

    @Override
    public PerspectiveModelState getModelState() {
        return TransformUtils.DEFAULT_BLOCK;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }
}
