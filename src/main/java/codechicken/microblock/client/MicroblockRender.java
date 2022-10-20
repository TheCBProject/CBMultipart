package codechicken.microblock.client;

import codechicken.lib.model.CachedFormat;
import codechicken.lib.model.pipeline.BakedPipeline;
import codechicken.lib.model.pipeline.transformers.QuadClamper;
import codechicken.lib.model.pipeline.transformers.QuadFaceStripper;
import codechicken.lib.model.pipeline.transformers.QuadReInterpolator;
import codechicken.lib.model.pipeline.transformers.QuadTinter;
import codechicken.lib.render.BakedVertexSource;
import codechicken.lib.render.CCRenderState;
import codechicken.microblock.util.MaskedCuboid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

/**
 * Created by covers1624 on 20/10/22.
 */
public class MicroblockRender {

    private static final ThreadLocal<PipelineState> PIPELINES = ThreadLocal.withInitial(PipelineState::create);

    public static boolean renderCuboids(CCRenderState ccrs, BlockState state, RenderType layer, List<MaskedCuboid> cuboids) {
        PipelineState pipeState = PIPELINES.get();
        BakedVertexSource vertexSource = BakedVertexSource.instance();
        Random randy = new Random();

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        BakedModel model = dispatcher.getBlockModel(state);

        BlockPos pos = null;
        long seed = 42L;
        BlockAndTintGetter level = null;
        IModelData modelData = EmptyModelData.INSTANCE;
        if (layer != null) {
            pos = ccrs.lightMatrix.pos;
            seed = state.getSeed(pos);
            level = new MicroblockLevelProxy(ccrs.lightMatrix.access, pos, state);
            modelData = model.getModelData(level, pos, state, modelData);
        }

        boolean ret = false;
        for (Direction face : Direction.BY_3D_DATA) {
            randy.setSeed(seed);
            for (BakedQuad quad : model.getQuads(state, face, randy, modelData)) {
                ret |= renderQuad(ccrs, vertexSource, pipeState, blockColors, layer, quad, state, level, pos, cuboids);
            }
        }
        randy.setSeed(seed);
        for (BakedQuad quad : model.getQuads(state, null, randy, modelData)) {
            ret |= renderQuad(ccrs, vertexSource, pipeState, blockColors, layer, quad, state, level, pos, cuboids);
        }
        return ret;
    }

    private static boolean renderQuad(CCRenderState ccrs, BakedVertexSource vs, PipelineState pipeState, BlockColors blockColors, RenderType layer, BakedQuad quad, BlockState state, BlockAndTintGetter level, BlockPos pos, List<MaskedCuboid> cuboids) {
        BakedPipeline pipeline = pipeState.pipeline;
        QuadClamper clamper = pipeState.clamper;
        QuadFaceStripper faceStripper = pipeState.faceStripper;
        QuadTinter tinter = pipeState.tinter;

        CachedFormat format = formatFor(quad);
        vs.reset(format);

        if (quad.isTinted()) {
            tinter.setTint(blockColors.getColor(state, level, pos, quad.getTintIndex()));
        }
        for (MaskedCuboid cuboid : cuboids) {
            pipeline.reset(format);
            faceStripper.setBounds(cuboid.box());
            faceStripper.setMask(cuboid.sideMask());
            clamper.setClampBounds(cuboid.box());

            pipeline.setElementState("tinter", quad.isTinted());

            pipeline.prepare(vs);
            quad.pipe(pipeline);
        }

        if (vs.availableVertices() <= 0) return false;

        ccrs.setModel(vs);
        if (layer != null) {
            ccrs.render(ccrs.lightMatrix);
        } else {
            ccrs.render();
        }
        return true;
    }

    // Exists as a mixin target for mods which change the vertex formats.
    private static CachedFormat formatFor(BakedQuad quad) {
        return CachedFormat.BLOCK;
    }

    private record PipelineState(BakedPipeline pipeline, QuadClamper clamper, QuadFaceStripper faceStripper, QuadTinter tinter) {

        public static PipelineState create() {
            BakedPipeline pipeline = BakedPipeline.builder()
                    .addElement("clamper", QuadClamper.FACTORY)
                    .addElement("face_stripper", QuadFaceStripper.FACTORY)
                    .addElement("interpolator", QuadReInterpolator.FACTORY)
                    .addElement("tinter", QuadTinter.FACTORY, false)
                    .build();

            return new PipelineState(
                    pipeline,
                    pipeline.getElement("clamper", QuadClamper.class),
                    pipeline.getElement("face_stripper", QuadFaceStripper.class),
                    pipeline.getElement("tinter", QuadTinter.class)
            );
        }
    }

    private static class MicroblockLevelProxy implements BlockAndTintGetter {

        private final BlockAndTintGetter other;
        private final BlockPos pos;
        private final BlockState state;

        private MicroblockLevelProxy(BlockAndTintGetter other, BlockPos pos, BlockState state) {
            this.other = other;
            this.pos = pos;
            this.state = state;
        }

        // @formatter:off
        @Override public float getShade(Direction face, boolean shade) { return other.getShade(face, shade); }
        @Override public LevelLightEngine getLightEngine() { return other.getLightEngine(); }
        @Override public int getBlockTint(BlockPos pos, ColorResolver resolver) { return other.getBlockTint(pos, resolver); }
        @Nullable @Override public BlockEntity getBlockEntity(BlockPos pos) { return other.getBlockEntity(pos); }
        @Override public BlockState getBlockState(BlockPos pos) { return pos.equals(this.pos) ? state : other.getBlockState(pos); }
        @Override public FluidState getFluidState(BlockPos pos) { return other.getFluidState(pos); }
        @Override public int getHeight() { return other.getHeight(); }
        @Override public int getMinBuildHeight() { return other.getMinBuildHeight(); }
        // @formatter:on
    }
}
