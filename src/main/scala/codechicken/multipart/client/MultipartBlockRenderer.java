package codechicken.multipart.client;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.block.ICCBlockRenderer;
import codechicken.lib.render.buffer.TransformingVertexBuilder;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.BlockMultiPart;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.init.ModContent;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;

import java.util.Random;

/**
 * Created by covers1624 on 31/8/20.
 */
public class MultipartBlockRenderer implements ICCBlockRenderer {

    @Override
    public boolean canHandleBlock(ILightReader world, BlockPos pos, BlockState blockState) {
        return blockState.getBlock() == ModContent.blockMultipart;
    }

    @Override
    public boolean renderBlock(BlockState state, BlockPos pos, ILightReader world, MatrixStack mStack, IVertexBuilder builder, Random random, IModelData data) {
        TileMultiPart tile = BlockMultiPart.getTile(world, pos);
        if (tile != null) {
            CCRenderState ccrs = CCRenderState.instance();
            ccrs.reset();
            ccrs.bind(new TransformingVertexBuilder(builder, mStack), DefaultVertexFormats.BLOCK);
            ccrs.lightMatrix.locate(world, pos);
            return renderStatic(tile, MinecraftForgeClient.getRenderLayer(), ccrs);
        }
        return false;
    }

    @Override
    public void renderBreaking(BlockState state, BlockPos pos, ILightReader world, MatrixStack mStack, IVertexBuilder builder, IModelData data) {
        TileMultiPart tile = BlockMultiPart.getTile(world, pos);
        if (tile != null) {
            CCRenderState ccrs = CCRenderState.instance();
            ccrs.reset();
            mStack.push();
            ccrs.bind(new TransformingVertexBuilder(builder, mStack), DefaultVertexFormats.BLOCK);
            ccrs.overlay = OverlayTexture.NO_OVERLAY;
            ccrs.brightness = WorldRenderer.getPackedLightmapCoords(world, state, pos);
            ccrs.lightMatrix.locate(world, pos);
            renderStatic(tile, MinecraftForgeClient.getRenderLayer(), ccrs);
            mStack.pop();
        }
    }

    private boolean renderStatic(TileMultiPart tile, RenderType type, CCRenderState ccrs) {
        boolean ret = false;
        for (TMultiPart part : tile.getPartList()) {
            ret |= part.renderStatic(type, ccrs);
        }
        return ret;
    }
}
