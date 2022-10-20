package codechicken.multipart;

import codechicken.lib.world.TileChunkLoadHook;
import codechicken.multipart.handler.PlacementConversionHandler;
import codechicken.multipart.init.CBMultipartModContent;
import codechicken.multipart.init.ClientInit;
import codechicken.multipart.init.DataGenerators;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.network.MultiPartNetwork;
import codechicken.multipart.util.MultiPartGenerator;
import codechicken.multipart.util.MultiPartLoadHandler;
import codechicken.multipart.util.TickScheduler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static codechicken.multipart.CBMultipart.MOD_ID;

/**
 * Created by covers1624 on 30/8/20.
 */
@Mod (MOD_ID)
public class CBMultipart {

    public static final String MOD_ID = "cb_multipart";

    public CBMultipart() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        CBMultipartModContent.init(modEventBus);
        MultiPartRegistries.init(modEventBus);
        DataGenerators.init(modEventBus);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientInit::init);

        MultiPartGenerator.INSTANCE.loadAnnotations();
        MultiPartLoadHandler.init();
        MultiPartNetwork.init();
        PlacementConversionHandler.init();
        TickScheduler.init();
        TileChunkLoadHook.init();
    }
}
