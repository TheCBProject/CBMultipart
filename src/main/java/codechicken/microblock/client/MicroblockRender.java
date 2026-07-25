package codechicken.microblock.client;

import codechicken.lib.model.*;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.api.MicroMaterialClient;
import codechicken.microblock.client.MicroblockPlacementOutlineRenderer.PlacementPreview;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.item.MicroMaterialComponent;
import codechicken.microblock.part.MicroblockPlacement;
import codechicken.microblock.util.MaskedCuboid;
import codechicken.multipart.util.ProxyBlockAndTintGetter;
import codechicken.multipart.util.Sides;
import codechicken.multipart.util.WrappedBlockModelPart;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.model.data.ModelData;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static codechicken.microblock.CBMicroblock.MOD_ID;

/**
 * Created by covers1624 on 20/10/22.
 */
public class MicroblockRender {

    private static final CrashLock LOCK = new CrashLock("Already Initialized");
    private static final ThreadLocal<MicroblockQuadPipeline> PIPELINES = ThreadLocal.withInitial(MicroblockQuadPipeline::new);

    private static final Cache<CacheKey, List<BlockModelPart>> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build();

    public static void init(IEventBus modBus) {
        LOCK.lock();

        modBus.addListener(MicroblockRender::onRegisterReloadListeners);

        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, MicroblockRender::onDrawHighlight);
    }

    private static void onRegisterReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(
                Identifier.fromNamespaceAndPath(MOD_ID, "quad_cache"),
                (ResourceManagerReloadListener) e -> CACHE.invalidateAll()
        );
    }

    private static void onDrawHighlight(ExtractBlockOutlineRenderStateEvent event) {
        if (!(event.getCamera().entity() instanceof Player player)) return;

        // TODO offhand?
        var stack = player.getMainHandItem();
        if (!stack.is(CBMicroblockModContent.MICRO_BLOCK_ITEM.get())) return;

        MicroMaterialComponent component = MicroMaterialComponent.getComponent(stack);
        if (component == null || component.factory() == null) return;
        var material = component.material();
        var factory = component.factory();
        var size = component.size();

        var materialClient = MicroMaterialClient.get(material);

        var hit = event.getHitResult();
        var placementProps = factory.placementProperties();
        var placementGrid = placementProps.placementGrid();
        var placement = new MicroblockPlacement(player, InteractionHand.MAIN_HAND, hit, size, material, !player.getAbilities().instabuild, placementProps)
                .calculate();

        PlacementPreview preview = null;
        if (placement != null && materialClient != null) {
            List<BlockModelPart> parts = new ArrayList<>();
            materialClient.collectParts(player.level(), placement.pos, placement.part.getRenderCuboids(true), parts);
            preview = new PlacementPreview(placement.pos, parts);
        }

        // Must create new BlockOutlineRenderState, as we need to cancel the envent outright.
        event.getLevelRenderState().blockOutlineRenderState = new BlockOutlineRenderState(
                event.getBlockPos(),
                event.isInTranslucentPass(),
                event.isHighContrast(),
                Shapes.empty(),
                List.of(new MicroblockPlacementOutlineRenderer(
                        new Vector3(hit.getLocation()),
                        hit.getDirection().ordinal(),
                        placementGrid.getOverlayLines(),
                        preview
                ))
        );
        event.setCanceled(true);

    }

    public static void collectParts(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, BlockState state, ImmutableSet<MaskedCuboid> renderCuboids, List<BlockModelPart> partsOutput) {
        MicroblockQuadPipeline pipeline = PIPELINES.get();
        RandomSource randy = RandomSource.create();

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        var model = dispatcher.getBlockModel(state);

        long seed = 42L;
        if (level != null) {
            assert pos != null;
            seed = state.getSeed(pos);
            level = new ProxyBlockAndTintGetter(level, pos, state, ModelData.EMPTY);
        }

        // TODO dynamic tinting system for parts.
        boolean mayHaveTint = Minecraft.getInstance().getBlockColors().blockColors.containsKey(state.getBlock());
        randy.setSeed(seed);
        var cacheKey = new CacheKey(
                state,
                renderCuboids,
                model.createGeometryKey(levelOrEmpty(level), posOrZero(pos), state, randy)
        );

        var output = partsOutput;
        if (cacheKey.geometryCacheKey != null && !mayHaveTint) {
            synchronized (CACHE) {
                var existing = CACHE.getIfPresent(cacheKey);
                if (existing != null) {
                    partsOutput.addAll(existing);
                    return;
                }
            }
            output = new ArrayList<>();
        }

        randy.setSeed(seed);
        pipeline.transform(model, level, pos, state, randy, renderCuboids, output);

        if (cacheKey.geometryCacheKey != null) {
            synchronized (CACHE) {
                CACHE.put(cacheKey, output);
            }
            partsOutput.addAll(output);
        }
    }

    private static BlockAndTintGetter levelOrEmpty(@Nullable BlockAndTintGetter level) {
        return level != null ? level : EmptyBlockAndTintGetter.INSTANCE;
    }

    private static BlockPos posOrZero(@Nullable BlockPos pos) {
        return pos != null ? pos : BlockPos.ZERO;
    }

    private record CacheKey(BlockState state, ImmutableSet<MaskedCuboid> cuboids, @Nullable Object geometryCacheKey) { }

    private static final class MicroblockQuadPipeline {

        private final List<BlockModelPart> collector = new ObjectArrayList<>();
        private final Quad mutableQuad = new Quad();
        private final QuadClamper clamper = new QuadClamper();
        private final QuadFaceStripper faceStripper = new QuadFaceStripper();
        private final QuadReInterpolator reInterpolator = new QuadReInterpolator();
        private final QuadTinter tinter = new QuadTinter();

        public void transform(BlockStateModel model, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, BlockState state, RandomSource randy, Iterable<MaskedCuboid> renderCuboids, List<BlockModelPart> output) {
            collector.clear();
            model.collectParts(levelOrEmpty(level), posOrZero(pos), state, randy, collector);
            for (var modelPart : collector) {
                var quads = transformPart(modelPart, level, pos, state, renderCuboids);
                output.add(new WrappedBlockModelPart(modelPart, state) {
                    @Override
                    public List<BakedQuad> getQuads(@Nullable Direction side) {
                        return side == null ? quads : List.of();
                    }
                });
            }
            collector.clear();
        }

        private List<BakedQuad> transformPart(BlockModelPart modelPart, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, BlockState state, Iterable<MaskedCuboid> renderCuboids) {
            BlockColors blockColors = Minecraft.getInstance().getBlockColors();

            ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();
            for (var side : Sides.SIDES_AND_NULL) {
                for (var quad : modelPart.getQuads(side)) {
                    int tint = quad.isTinted() ? blockColors.getColor(state, level, pos, quad.tintIndex()) : -1;
                    transformQuad(quad, tint, renderCuboids, quads);
                }
            }
            return quads.build();
        }

        private void transformQuad(BakedQuad quad, int tint, Iterable<MaskedCuboid> renderCuboids, ImmutableList.Builder<BakedQuad> quads) {
            tinter.setTint(tint);

            for (var cuboid : renderCuboids) {
                mutableQuad.copyFrom(quad);
                faceStripper.setBounds(cuboid.box());
                faceStripper.setMask(cuboid.sideMask());
                clamper.setClampBounds(cuboid.box());
                if (QuadTransformer.transform(mutableQuad, clamper, faceStripper, reInterpolator, tinter)) {
                    quads.add(mutableQuad.bake());
                }
            }
        }
    }
}
