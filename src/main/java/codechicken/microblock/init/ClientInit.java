package codechicken.microblock.init;

import codechicken.microblock.client.MicroblockPartRenderer;
import codechicken.microblock.client.MicroblockItemRenderer;
import codechicken.microblock.client.MicroblockRender;
import codechicken.multipart.api.MultipartClientRegistry;
import net.covers1624.quack.util.CrashLock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

/**
 * Created by covers1624 on 20/10/22.
 */
public class ClientInit {

    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    public static void init(IEventBus modBus) {
        LOCK.lock();

        modBus.addListener(ClientInit::clientSetup);
        modBus.addListener(ClientInit::onRegisterSpecialModelRenderers);

        MicroblockRender.init(modBus);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        MultipartClientRegistry.register(CBMicroblockModContent.FACE_MICROBLOCK_PART.get(), MicroblockPartRenderer.INSTANCE);
        MultipartClientRegistry.register(CBMicroblockModContent.HOLLOW_MICROBLOCK_PART.get(), MicroblockPartRenderer.INSTANCE);
        MultipartClientRegistry.register(CBMicroblockModContent.CORNER_MICROBLOCK_PART.get(), MicroblockPartRenderer.INSTANCE);
        MultipartClientRegistry.register(CBMicroblockModContent.EDGE_MICROBLOCK_PART.get(), MicroblockPartRenderer.INSTANCE);
        MultipartClientRegistry.register(CBMicroblockModContent.POST_MICROBLOCK_PART.get(), MicroblockPartRenderer.INSTANCE);
    }

    private static void onRegisterSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(MicroblockItemRenderer.Unbaked.TYPE, MicroblockItemRenderer.Unbaked.CODEC);
    }
}
