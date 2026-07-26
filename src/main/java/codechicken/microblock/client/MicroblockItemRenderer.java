package codechicken.microblock.client;

import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.api.MicroMaterialClient;
import codechicken.microblock.item.MicroMaterialComponent;
import codechicken.multipart.init.CBMultipartModContent;
import codechicken.multipart.util.Sides;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.RenderTypeHelper;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static codechicken.microblock.CBMicroblock.MOD_ID;

/**
 * Created by covers1624 on 20/10/22.
 */
public class MicroblockItemRenderer implements SpecialModelRenderer<MicroMaterialComponent> {

    @Override
    @Nullable
    public MicroMaterialComponent extractArgument(ItemStack stack) {
        return MicroMaterialComponent.getComponent(stack);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        // Just assume this item is a full block.
        // There isn't really much more we can do here without context.
        output.accept(Cuboid6.full.min.vector3f());
        output.accept(Cuboid6.full.max.vector3f());
    }

    @Override
    public void submit(@Nullable MicroMaterialComponent component, ItemDisplayContext displayContext, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int packedOverlay, boolean hasFoil, int outlineColor) {
        if (component == null || component.factory() == null) return;

        var clientMaterial = MicroMaterialClient.get(component.material());
        if (clientMaterial == null) return;

        var part = component.factory().create(true, component.material());
        part.setShape(component.size(), component.factory().getItemSlot());

        poseStack.pushPose();
        var offset = Vector3.CENTER.copy().subtract(part.getBounds().center());
        poseStack.translate(offset.x, offset.y, offset.z);

        var ourState = CBMultipartModContent.MULTIPART_BLOCK.get().defaultBlockState();
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();

        var cuboids = part.getRenderCuboids(true);
        List<BlockModelPart> list = new ArrayList<>();
        clientMaterial.collectParts(null, null, cuboids, list);
        for (var modelPart : list) {
            // The state used for getRenderType here is not important, all MicroMaterial instances should
            // wrap BlockModelPart capturing the real block state.
            // In-world rendering would pass in our own BlockMultipart state anyway.
            var renderType = modelPart.getRenderType(ourState);
            collector.submitCustomGeometry(poseStack, RenderTypeHelper.getEntityRenderType(renderType), (pose, consumer) -> {
                int lastTint = -1;
                int lastTintIndex = -1;
                for (var side : Sides.SIDES_AND_NULL) {
                    for (BakedQuad quad : modelPart.getQuads(side)) {
                        float r = 1.0F;
                        float g = 1.0F;
                        float b = 1.0F;
                        if (quad.isTinted()) {
                            if (lastTintIndex != quad.tintIndex()) {
                                lastTint = blockColors.getColor(ourState, null, null, quad.tintIndex());
                                lastTintIndex = quad.tintIndex();
                            }
                            r = ARGB.redFloat(lastTint);
                            g = ARGB.greenFloat(lastTint);
                            b = ARGB.blueFloat(lastTint);
                        }
                        consumer.putBulkData(pose, quad, r, g, b, 1.0F, packedLight, packedOverlay);
                    }
                }
            });
        }

        // TODO
//        clientMaterial.submitDynamic(part, displayContext, poseStack, collector, packedLight, packedOverlay, hasFoil, outlineColor);
        poseStack.popPose();
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {

        public static final Identifier TYPE = Identifier.fromNamespaceAndPath(MOD_ID, "microblock");
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public SpecialModelRenderer<?> bake(BakingContext context) {
            return new MicroblockItemRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return CODEC;
        }
    }
}
