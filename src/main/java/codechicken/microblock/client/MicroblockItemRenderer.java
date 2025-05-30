package codechicken.microblock.client;

import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.api.MicroMaterialClient;
import codechicken.microblock.item.MicroMaterialComponent;
import codechicken.microblock.part.MicroblockPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Created by covers1624 on 20/10/22.
 */
public class MicroblockItemRenderer implements IItemRenderer {

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack mStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        MicroMaterialComponent component = MicroMaterialComponent.getComponent(stack);

        if (component == null || component.factory() == null) return;

        MicroMaterialClient clientMaterial = MicroMaterialClient.get(component.material());
        if (clientMaterial == null) return;

        MicroblockPart part = component.factory().create(true, component.material());
        part.setShape(component.size(), component.factory().getItemSlot());

        mStack.pushPose();
        Vector3 offset = Vector3.CENTER.copy().subtract(part.getBounds().center());
        mStack.translate(offset.x, offset.y, offset.z);

        RenderType layer = clientMaterial.getItemRenderLayer();
        var cuboids = part.getRenderCuboids(true);
        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        for (Direction side : Direction.values()) {
            itemRenderer.renderQuadList(
                    mStack,
                    buffers.getBuffer(layer),
                    clientMaterial.getQuads(part, side, null, cuboids),
                    stack,
                    packedLight,
                    packedOverlay
            );
        }
        itemRenderer.renderQuadList(
                mStack,
                buffers.getBuffer(layer),
                clientMaterial.getQuads(part, null, null, cuboids),
                stack,
                packedLight,
                packedOverlay
        );

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
