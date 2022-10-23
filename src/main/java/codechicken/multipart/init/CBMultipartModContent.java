package codechicken.multipart.init;

import codechicken.multipart.block.BlockMultiPart;
import codechicken.multipart.util.MultiPartLoadHandler.TileNBTContainer;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static codechicken.multipart.CBMultipart.MOD_ID;

/**
 * Created by covers1624 on 2/9/20.
 */

public class CBMultipartModContent {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registry.BLOCK_REGISTRY, MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registry.BLOCK_ENTITY_TYPE_REGISTRY, MOD_ID);

    public static final RegistryObject<BlockMultiPart> MULTIPART_BLOCK = BLOCKS.register("multipart", BlockMultiPart::new);

    public static final RegistryObject<BlockEntityType<?>> MULTIPART_TILE_TYPE = TILES.register("saved_multipart", () ->
            BlockEntityType.Builder.of(TileNBTContainer::new, MULTIPART_BLOCK.get()).build(null));

    public static void init() {
        LOCK.lock();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        TILES.register(bus);
    }
}
