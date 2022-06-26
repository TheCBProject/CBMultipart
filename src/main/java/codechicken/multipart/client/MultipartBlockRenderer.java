package codechicken.multipart.client;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.block.ICCBlockRenderer;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.render.PartRenderer;
import codechicken.multipart.block.BlockMultiPart;
import codechicken.multipart.block.TileMultiPart;
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
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;

import java.util.Random;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 31/8/20.
 */
public class MultipartBlockRenderer implements ICCBlockRenderer {

    @Override
    public boolean canHandleBlock(BlockAndTintGetter world, BlockPos pos, BlockState blockState) {
        return blockState.getBlock() == CBMultipartModContent.blockMultipart;
    }

    @Override
    public boolean renderBlock(BlockState state, BlockPos pos, BlockAndTintGetter world, PoseStack mStack, VertexConsumer builder, Random random, IModelData data) {
        TileMultiPart tile = BlockMultiPart.getTile(world, pos);
        if (tile != null) {
            CCRenderState ccrs = CCRenderState.instance();
            ccrs.reset();
            ccrs.bind(new TransformingVertexConsumer(builder, mStack), DefaultVertexFormat.BLOCK);
            ccrs.lightMatrix.locate(world, pos);
            return renderStatic(tile, MinecraftForgeClient.getRenderType(), ccrs);
        }
        return false;
    }

    @Override
    public void renderBreaking(BlockState state, BlockPos pos, BlockAndTintGetter world, PoseStack mStack, VertexConsumer builder, IModelData data) {
        TileMultiPart tile = BlockMultiPart.getTile(world, pos);
        if (tile != null) {
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
    }

    private boolean renderStatic(TileMultiPart tile, RenderType type, CCRenderState ccrs) {
        boolean ret = false;
        for (TMultiPart part : tile.getPartList()) {
            PartRenderer<?> renderer = MultipartClientRegistry.getRenderer(part.getType());
            if (renderer != null) {
                ret |= renderer.renderStatic(unsafeCast(part), type, ccrs);
            }
        }
        return ret;
    }

    private void renderBreaking(TileMultiPart tile, CCRenderState ccrs) {
        if (Minecraft.getInstance().hitResult instanceof PartRayTraceResult hit) {
            PartRenderer<?> renderer = MultipartClientRegistry.getRenderer(hit.part.getType());
            if (renderer != null) {
                renderer.renderBreaking(unsafeCast(hit.part), ccrs);
            }
        }
    }
}
