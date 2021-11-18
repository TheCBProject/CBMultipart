package codechicken.multipart;

import codechicken.multipart.init.CBMultipartModContent;
import codechicken.multipart.init.DataGenerators;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.proxy.Proxy;
import codechicken.multipart.proxy.ProxyClient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.scorge.lang.ScorgeModLoadingContext;

/**
 * Created by covers1624 on 30/8/20.
 */
public class CBMultipart {

    public static final String MOD_ID = "cb_multipart";

    public static Proxy proxy;

    public CBMultipart() {
        proxy = DistExecutor.safeRunForDist(() -> ProxyClient::new, () -> Proxy::new);
        ScorgeModLoadingContext.get().getModEventBus().register(this);
        CBMultipartModContent.init(ScorgeModLoadingContext.get().getModEventBus());
        MultiPartRegistries.init(ScorgeModLoadingContext.get().getModEventBus());
        DataGenerators.init(ScorgeModLoadingContext.get().getModEventBus());
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
