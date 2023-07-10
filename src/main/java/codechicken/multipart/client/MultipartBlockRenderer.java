package codechicken.multipart.client;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.block.ICCBlockRenderer;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.render.PartRenderer;
import codechicken.multipart.block.BlockMultipart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.init.CBMultipartModContent;
import codechicken.multipart.util.PartRayTraceResult;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 31/8/20.
 */
public class MultipartBlockRenderer implements ICCBlockRenderer {

    @Override
    public boolean canHandleBlock(BlockAndTintGetter world, BlockPos pos, BlockState blockState, @Nullable RenderType renderType) {
        return blockState.getBlock() == CBMultipartModContent.MULTIPART_BLOCK.get();
    }

    @Override
    public void renderBlock(BlockState state, BlockPos pos, BlockAndTintGetter world, PoseStack mStack, VertexConsumer builder, RandomSource random, ModelData data, @Nullable RenderType renderType) {
        TileMultipart tile = BlockMultipart.getTile(world, pos);
        if (tile == null) return;

        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.bind(new TransformingVertexConsumer(builder, mStack), DefaultVertexFormat.BLOCK);
        ccrs.lightMatrix.locate(world, pos);
        renderStatic(tile, renderType, ccrs);
    }

    @Override
    public void renderBreaking(BlockState state, BlockPos pos, BlockAndTintGetter world, PoseStack mStack, VertexConsumer builder, ModelData data) {
        TileMultipart tile = BlockMultipart.getTile(world, pos);
        if (tile == null) return;

        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        mStack.pushPose();
        ccrs.bind(new TransformingVertexConsumer(builder, mStack), DefaultVertexFormat.BLOCK);
        ccrs.overlay = OverlayTexture.NO_OVERLAY;
        ccrs.brightness = LevelRenderer.getLightColor(world, state, pos);
        ccrs.lightMatrix.locate(world, pos);
        renderBreaking(tile, ccrs);
        mStack.popPose();
    }

    private void renderStatic(TileMultipart tile, @Nullable RenderType type, CCRenderState ccrs) {
        for (MultiPart part : tile.getPartList()) {
            PartRenderer<?> renderer = MultipartClientRegistry.getRenderer(part.getType());
            if (renderer != null) {
                renderer.renderStatic(unsafeCast(part), type, ccrs);
            }
        }
    }

    private void renderBreaking(TileMultipart tile, CCRenderState ccrs) {
        if (Minecraft.getInstance().hitResult instanceof PartRayTraceResult hit) {
            PartRenderer<?> renderer = MultipartClientRegistry.getRenderer(hit.part.getType());
            if (renderer != null) {
                renderer.renderBreaking(unsafeCast(hit.part), ccrs);
            }
        }
    }
}
