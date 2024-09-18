package codechicken.multipart.init;

import codechicken.multipart.block.BlockMultipart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.util.MultipartLoadHandler.TileNBTContainer;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.registries.Registries;
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

    public static final DeferredHolder<Block, BlockMultipart> MULTIPART_BLOCK = BLOCKS.register("multipart", BlockMultipart::new);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> MULTIPART_TILE_TYPE = TILES.register("saved_multipart", () ->
            BlockEntityType.Builder.of(TileNBTContainer::new, MULTIPART_BLOCK.get()).build(null));

    public static void init(IEventBus modBus) {
        LOCK.lock();
        BLOCKS.register(modBus);
        TILES.register(modBus);

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
