package codechicken.microblock.init;

import codechicken.microblock.client.MicroBlockPartRenderer;
import codechicken.microblock.client.MicroblockRender;
import codechicken.multipart.api.MultipartClientRegistry;
import net.covers1624.quack.util.CrashLock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Created by covers1624 on 20/10/22.
 */
public class ClientInit {

    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

//    private static final ModelRegistryHelper MODEL_HELPER = new ModelRegistryHelper();

    public static void init(IEventBus modBus) {
        LOCK.lock();

        modBus.addListener(ClientInit::clientSetup);

        MicroblockRender.init(modBus);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        MultipartClientRegistry.register(CBMicroblockModContent.FACE_MICROBLOCK_PART.get(), MicroBlockPartRenderer.INSTANCE);
        MultipartClientRegistry.register(CBMicroblockModContent.HOLLOW_MICROBLOCK_PART.get(), MicroBlockPartRenderer.INSTANCE);
        MultipartClientRegistry.register(CBMicroblockModContent.CORNER_MICROBLOCK_PART.get(), MicroBlockPartRenderer.INSTANCE);
        MultipartClientRegistry.register(CBMicroblockModContent.EDGE_MICROBLOCK_PART.get(), MicroBlockPartRenderer.INSTANCE);
        MultipartClientRegistry.register(CBMicroblockModContent.POST_MICROBLOCK_PART.get(), MicroBlockPartRenderer.INSTANCE);
//        MODEL_HELPER.register(new ModelResourceLocation(CBMicroblockModContent.MICRO_BLOCK_ITEM.getId(), "inventory"), new MicroblockItemRenderer());
    }
}
