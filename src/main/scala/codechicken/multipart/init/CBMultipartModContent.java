package codechicken.multipart.init;

import codechicken.lib.util.CrashLock;
import codechicken.multipart.block.BlockMultiPart;
import codechicken.multipart.util.MultiPartLoadHandler.TileNBTContainer;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
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
    public static TileEntityType<?> tileMultipartType;

    public static void init(IEventBus eventBus) {
        LOCK.lock();
        eventBus.addGenericListener(Block.class, CBMultipartModContent::onRegisterBlocks);
        eventBus.addGenericListener(TileEntityType.class, CBMultipartModContent::onRegisterTiles);
    }

    private static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> r = event.getRegistry();
        r.register(new BlockMultiPart().setRegistryName("multipart"));
    }

    private static void onRegisterTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        IForgeRegistry<TileEntityType<?>> r = event.getRegistry();
        r.register(TileEntityType.Builder.of(TileNBTContainer::new, blockMultipart).build(null)
                .setRegistryName("saved_multipart")
        );
    }

}
