package codechicken.microblock.client;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.api.BlockMicroMaterial;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.factory.StandardMicroFactory;
import codechicken.microblock.item.ItemMicroBlock;
import codechicken.microblock.part.MicroblockPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.world.item.ItemStack;

/**
 * Created by covers1624 on 20/10/22.
 */
public class MicroblockItemRenderer implements IItemRenderer {

    @Override
    public void renderItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack mStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        MicroMaterial material = ItemMicroBlock.getMaterialFromStack(stack);
        StandardMicroFactory<?> factory = ItemMicroBlock.getFactory(stack);
        int size = ItemMicroBlock.getSize(stack);

        if (material == null || factory == null) return;

        MicroblockPart part = factory.create(true, material);
        part.setShape(size, factory.getItemSlot());

        Matrix4 mat = new Matrix4(mStack);
        mat.translate(Vector3.CENTER.copy().subtract(part.getBounds().center()));

        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;

        RenderType layer = part.getMaterial().getItemRenderLayer();
        ccrs.bind(layer, buffers, mat);
        MicroblockRender.renderCuboids(ccrs, ((BlockMicroMaterial) material).state, null, part.getRenderCuboids(true));
    }

    @Override
    public ModelState getModelTransform() {
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
