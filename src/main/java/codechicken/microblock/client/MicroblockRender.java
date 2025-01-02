package codechicken.microblock.client;

import codechicken.lib.model.CachedFormat;
import codechicken.lib.model.pipeline.BakedPipeline;
import codechicken.lib.model.pipeline.transformers.QuadClamper;
import codechicken.lib.model.pipeline.transformers.QuadFaceStripper;
import codechicken.lib.model.pipeline.transformers.QuadReInterpolator;
import codechicken.lib.model.pipeline.transformers.QuadTinter;
import codechicken.lib.render.BakedVertexSource;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.lib.vec.*;
import codechicken.microblock.api.BlockMicroMaterial;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.item.ItemMicroBlock;
import codechicken.microblock.item.MicroMaterialComponent;
import codechicken.microblock.part.ExecutablePlacement;
import codechicken.microblock.part.MicroblockPlacement;
import codechicken.microblock.part.PlacementGrid;
import codechicken.microblock.part.StandardMicroFactory;
import codechicken.microblock.util.MaskedCuboid;
import codechicken.multipart.client.Shaders;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;

import static codechicken.microblock.CBMicroblock.MOD_ID;

/**
 * Created by covers1624 on 20/10/22.
 */
public class MicroblockRender {

    private static final CrashLock LOCK = new CrashLock("Already Initialized");
    private static final ThreadLocal<PipelineState> PIPELINES = ThreadLocal.withInitial(PipelineState::create);

    public static final RenderType HIGHLIGHT_RENDER_TYPE = RenderType.create(MOD_ID + ":highlight", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 255, RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(Shaders::highlightShader))
            .setTextureState(RenderType.BLOCK_SHEET)
            .setWriteMaskState(RenderType.COLOR_DEPTH_WRITE)
            .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false)
    );

    private static final RenderType LINES = RenderType.create("lines", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, RenderType.CompositeState.builder()
            .setShaderState(RenderType.RENDERTYPE_LINES_SHADER)
            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(2)))
            .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
            .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
            .setWriteMaskState(RenderType.COLOR_DEPTH_WRITE)
            .setCullState(RenderType.NO_CULL)
            .createCompositeState(false)
    );

    public static void init() {
        LOCK.lock();

        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, MicroblockRender::onDrawHighlight);
    }

    private static void onDrawHighlight(RenderHighlightEvent.Block event) {
        Camera camera = event.getCamera();
        if (camera.getEntity() instanceof Player player) {
            ItemStack stack = player.getMainHandItem();
            // TODO offhand?

            // Nothing to do.
            if (!stack.is(CBMicroblockModContent.MICRO_BLOCK_ITEM.get())) return;

            PoseStack pStack = event.getPoseStack();
            pStack.pushPose();
            pStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
            if (renderHighlight(player, InteractionHand.MAIN_HAND, stack, event.getTarget(), pStack, event.getMultiBufferSource(), event.getDeltaTracker().getGameTimeDeltaPartialTick(true))) {
                event.setCanceled(true);
            }

            pStack.popPose();
        }
    }

    private static boolean renderHighlight(Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, PoseStack pStack, MultiBufferSource buffers, float partialTicks) {
        MicroMaterialComponent component = MicroMaterialComponent.getComponent(stack);
        if (component == null || component.factory() == null) return false;

        renderHighlight(player, hand, hit, component.factory(), component.size(), component.material(), pStack, buffers, partialTicks);
        return true;
    }

    private static void renderHighlight(Player player, InteractionHand hand, BlockHitResult hit, StandardMicroFactory factory, int size, MicroMaterial material, PoseStack pStack, MultiBufferSource buffers, float partialTicks) {
        if (MicroMaterialClientRegistry.renderHighlightOverride(player, hand, hit, factory, size, material, pStack, buffers, partialTicks)) {
            return;
        }

        renderPlacementGrid(factory.placementProperties().placementGrid(), pStack, new Vector3(hit.getLocation()), hit.getDirection().ordinal(), buffers);

        ExecutablePlacement placement = new MicroblockPlacement(player, hand, hit, size, material, !player.getAbilities().instabuild, factory.placementProperties()).calculate();
        if (placement == null) return;

        BlockPos pos = placement.pos;

        Matrix4 mat = new Matrix4(pStack);
        mat.translate(pos);
        mat.apply(new Scale(1.002, 1.002, 1.002).at(Vector3.CENTER));

        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.bind(HIGHLIGHT_RENDER_TYPE, buffers, mat);
        ccrs.alphaOverride = 80;
        renderCuboids(ccrs, ((BlockMicroMaterial) material).state, null, placement.part.getRenderCuboids(true));
    }

    private static void renderPlacementGrid(PlacementGrid grid, PoseStack pStack, Vector3 hit, int side, MultiBufferSource buffers) {
        Matrix4 mat = new Matrix4(pStack);
        transformFace(hit, side, mat);
        VertexConsumer cons = new TransformingVertexConsumer(buffers.getBuffer(LINES), mat);
        for (Line3 line : grid.getOverlayLines()) {
            bufferLinePair(cons, line.pt1, line.pt2, 0F, 0F, 0F, 1F);
        }
    }

    private static void bufferLinePair(VertexConsumer builder, Vector3 v1, Vector3 v2, float r, float g, float b, float a) {
        Vector3 vn = v1.copy().subtract(v2);
        double d = vn.mag();
        vn.divide(d);
        builder.addVertex((float) v1.x, (float) v1.y, (float) v1.z).setColor(r, g, b, a).setNormal((float) vn.x, (float) vn.y, (float) vn.z);
        builder.addVertex((float) v2.x, (float) v2.y, (float) v2.z).setColor(r, g, b, a).setNormal((float) vn.x, (float) vn.y, (float) vn.z);
    }

    private static void transformFace(Vector3 hit, int side, Matrix4 mat) {
        Vector3 pos = hit.copy().floor().add(Vector3.CENTER);
        mat.translate(pos);
        mat.apply(Rotation.sideRotations[side]);
        Vector3 rHit = pos.copy().subtract(hit).apply(Rotation.sideRotations[side ^ 1].inverse());
        mat.translate(0, rHit.y - 0.002, 0);
    }

    public static void renderCuboids(CCRenderState ccrs, BlockState state, @Nullable RenderType layer, Iterable<MaskedCuboid> cuboids) {
        PipelineState pipeState = PIPELINES.get();
        BakedVertexSource vertexSource = BakedVertexSource.instance();
        RandomSource randy = RandomSource.create();

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        BakedModel model = dispatcher.getBlockModel(state);

        BlockPos pos = null;
        long seed = 42L;
        BlockAndTintGetter level = null;
        ModelData modelData = ModelData.EMPTY;
        if (layer != null) {
            pos = ccrs.lightMatrix.pos;
            seed = state.getSeed(pos);
            level = new MicroblockLevelProxy(ccrs.lightMatrix.access, pos, state);
            modelData = model.getModelData(level, pos, state, modelData);

            if (!model.getRenderTypes(state, randy, modelData).contains(layer)) return;
        }

        for (Direction face : Direction.BY_3D_DATA) {
            randy.setSeed(seed);
            for (BakedQuad quad : model.getQuads(state, face, randy, modelData, layer)) {
                renderQuad(ccrs, vertexSource, pipeState, blockColors, layer, quad, state, level, pos, cuboids);
            }
        }
        randy.setSeed(seed);
        for (BakedQuad quad : model.getQuads(state, null, randy, modelData, layer)) {
            renderQuad(ccrs, vertexSource, pipeState, blockColors, layer, quad, state, level, pos, cuboids);
        }
    }

    private static void renderQuad(CCRenderState ccrs, BakedVertexSource vs, PipelineState pipeState, BlockColors blockColors, @Nullable RenderType layer, BakedQuad quad, BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, Iterable<MaskedCuboid> cuboids) {
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
            pipeline.put(quad);
        }

        if (vs.getVertexCount() <= 0) return;

        ccrs.setModel(vs);
        if (layer != null) {
            ccrs.render(ccrs.lightMatrix);
        } else {
            ccrs.render();
        }
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

    // @formatter:off
    private record MicroblockLevelProxy(BlockAndTintGetter other, BlockPos pos, BlockState state) implements BlockAndTintGetter {
        @Override public float getShade(Direction face, boolean shade) { return other.getShade(face, shade); }
        @Override public LevelLightEngine getLightEngine() { return other.getLightEngine(); }
        @Override public int getBlockTint(BlockPos pos, ColorResolver resolver) { return other.getBlockTint(pos, resolver); }
        @Nullable @Override public BlockEntity getBlockEntity(BlockPos pos) { return other.getBlockEntity(pos); }
        @Override public BlockState getBlockState(BlockPos pos) { return pos.equals(this.pos) ? state : other.getBlockState(pos); }
        @Override public FluidState getFluidState(BlockPos pos) { return other.getFluidState(pos); }
        @Override public int getHeight() { return other.getHeight(); }
        @Override public int getMinBuildHeight() { return other.getMinBuildHeight(); }
    }
    // @formatter:on
}
