package codechicken.multipart.client;

import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.render.PartRenderer;
import codechicken.multipart.api.part.render.PartRenderer.BEState;
import codechicken.multipart.block.TileMultipart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Created by covers1624 on 31/8/20.
 */
public class MultipartTileRenderer implements BlockEntityRenderer<BlockEntity, MultipartTileRenderer.RenderState> {

    public MultipartTileRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(BlockEntity be, RenderState state, float partialTick, Vec3 cameraPosition, @Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPosition, breakProgress);
        if (!(be instanceof TileMultipart tile)) return;

        var beState = new BEState(state.blockPos, state.lightCoords, state.breakProgress);
        List<PartState<?>> partStates = new ArrayList<>();
        for (MultiPart part : tile.getPartList()) {
            var renderer = MultipartClientRegistry.getRenderer(part.getType());
            if (renderer == null) continue;

            var partState = renderer.createDynamicState();
            if (partState == null) continue;

            renderer.extractDynamicState(beState, partState, part, partialTick, cameraPosition, breakProgress);
            partStates.add(new PartState<>(renderer, partState));
        }
        state.beState = beState;
        state.partStates = partStates;
    }

    @Override
    public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        if (state.partStates == null) return;
        requireNonNull(state.beState, "What?");

        for (var partState : state.partStates) {
            partState.submit(state.beState, poseStack, nodeCollector, cameraRenderState);
        }
    }

    //    @Override
//    public void render(BlockEntity t, float partialTicks, PoseStack mStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
//        if (!(t instanceof TileMultipart tile)) return;
//        CCRenderState ccrs = CCRenderState.instance();
//        ccrs.reset();
//        ccrs.brightness = packedLight;
//        ccrs.overlay = packedOverlay;
//        for (MultiPart p : tile.getPartList()) {
//            PartRenderer<?> renderer = MultipartClientRegistry.getRenderer(p.getType());
//            if (renderer != null) {
//                renderer.renderDynamic(unsafeCast(p), mStack, buffers, packedLight, packedOverlay, partialTicks);
//            }
//        }
//    }

    @Override
    public AABB getRenderBoundingBox(BlockEntity t) {
        if (!(t instanceof TileMultipart tile)) return new AABB(t.getBlockPos());

        Cuboid6 c = Cuboid6.full.copy();
        tile.operate(e -> c.enclose(e.getRenderBounds()));
        return c.add(tile.getBlockPos()).aabb();
    }

    public static class RenderState extends BlockEntityRenderState {

        public @Nullable BEState beState;
        public @Nullable List<PartState<?>> partStates;
    }

    public record PartState<S>(PartRenderer<?, S> renderer, S state) {

        void submit(BEState beState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
            renderer.submitDynamic(beState, state, poseStack, collector, cameraRenderState);
        }
    }
}
