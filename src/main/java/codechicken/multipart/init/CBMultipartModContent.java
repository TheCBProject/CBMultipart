package codechicken.multipart.init;

import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.PartConverter;
import codechicken.multipart.api.SimpleMultipartType;
import codechicken.multipart.block.BlockMultipart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.util.MultipartLoadHandler.TileNBTContainer;
import codechicken.multipart.wrapped.WrapperMultiPart;
import codechicken.multipart.wrapped.WrapperPartConverter;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static codechicken.multipart.CBMultipart.MOD_ID;
import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 2/9/20.
 */

public class CBMultipartModContent {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    private static final DeferredRegister<MultipartType<?>> PARTS = DeferredRegister.create(MultipartType.MULTIPART_TYPES, MOD_ID);
    private static final DeferredRegister<PartConverter> PART_CONVERTERS = DeferredRegister.create(PartConverter.PART_CONVERTERS, MOD_ID);

    public static final DeferredHolder<Block, BlockMultipart> MULTIPART_BLOCK = BLOCKS.register("multipart", BlockMultipart::new);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> MULTIPART_TILE_TYPE = TILES.register("saved_multipart", () ->
            BlockEntityType.Builder.of(TileNBTContainer::new, MULTIPART_BLOCK.get()).build(null));

    public static final DeferredHolder<MultipartType<?>, MultipartType<WrapperMultiPart>> WRAPPED_PART = PARTS.register("wrapper", () -> new SimpleMultipartType<>(e -> new WrapperMultiPart()));

    public static final DeferredHolder<PartConverter, WrapperPartConverter> WRAPPER_PART_CONVERTER = PART_CONVERTERS.register("wrapper", WrapperPartConverter::new);

    public static final TagKey<Block> ALLOW_MULTIPART_WRAPPING_TAG = BlockTags.create(ResourceLocation.fromNamespaceAndPath(MOD_ID, "allow_multipart_wrapping"));

    public static void init(IEventBus modBus) {
        LOCK.lock();
        BLOCKS.register(modBus);
        TILES.register(modBus);
        PARTS.register(modBus);
        PART_CONVERTERS.register(modBus);

        // TODO This may not be low enough, we can capture the event instance and do this in LoadComplete if it's an issue.
        modBus.addListener(EventPriority.LOWEST, CBMultipartModContent::onRegisterCapabilities);
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        for (BlockCapability<?, ?> cap : BlockCapability.getAll()) {
            event.registerBlockEntity(cap, CBMultipartModContent.MULTIPART_TILE_TYPE.get(), (tile, ctx) -> {
                if (tile instanceof TileMultipart t) {
                    return unsafeCast(t.getCapability(cap, unsafeCast(ctx)));
                }
                return null;
            });
        }
    }
}
