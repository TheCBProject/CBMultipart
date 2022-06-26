package codechicken.multipart.init;

import codechicken.multipart.block.BlockMultiPart;
import codechicken.multipart.util.MultiPartLoadHandler.TileNBTContainer;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import static codechicken.multipart.CBMultipart.MOD_ID;

/**
 * Created by covers1624 on 2/9/20.
 */
@ObjectHolder (MOD_ID)
public class CBMultipartModContent {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    @ObjectHolder ("multipart")
    public static BlockMultiPart blockMultipart;

    @ObjectHolder ("saved_multipart")
    public static BlockEntityType<?> tileMultipartType;

    public static void init(IEventBus eventBus) {
        LOCK.lock();
        eventBus.addGenericListener(Block.class, CBMultipartModContent::onRegisterBlocks);
        eventBus.addGenericListener(BlockEntityType.class, CBMultipartModContent::onRegisterTiles);
    }

    private static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> r = event.getRegistry();
        r.register(new BlockMultiPart().setRegistryName("multipart"));
    }

    private static void onRegisterTiles(RegistryEvent.Register<BlockEntityType<?>> event) {
        IForgeRegistry<BlockEntityType<?>> r = event.getRegistry();
        r.register(BlockEntityType.Builder.of(TileNBTContainer::new, blockMultipart).build(null)
                .setRegistryName("saved_multipart")
        );
    }

}
