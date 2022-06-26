package codechicken.multipart;

import codechicken.multipart.init.CBMultipartModContent;
import codechicken.multipart.init.DataGenerators;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.proxy.Proxy;
import codechicken.multipart.proxy.ProxyClient;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static codechicken.multipart.CBMultipart.MOD_ID;

/**
 * Created by covers1624 on 30/8/20.
 */
@Mod (MOD_ID)
public class CBMultipart {

    public static final String MOD_ID = "cb_multipart";

    public static Proxy proxy;

    public CBMultipart() {
        proxy = DistExecutor.safeRunForDist(() -> ProxyClient::new, () -> Proxy::new);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
        CBMultipartModContent.init(modEventBus);
        MultiPartRegistries.init(modEventBus);
        DataGenerators.init(modEventBus);
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        proxy.commonSetup(event);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        proxy.clientSetup(event);
    }
}
