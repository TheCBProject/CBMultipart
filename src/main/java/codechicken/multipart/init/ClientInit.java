package codechicken.multipart.init;

import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.multipart.client.ClientEventHandler;
import codechicken.multipart.client.MultipartTileRenderer;
import codechicken.multipart.client.Shaders;
import codechicken.multipart.handler.ControlKeyHandler;
import codechicken.multipart.wrapped.client.WrapperPartRenderer;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Created by covers1624 on 26/6/22.
 */
public class ClientInit {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    public static void init(IEventBus modBus) {
        LOCK.lock();

        ControlKeyHandler.init(modBus);
        ClientEventHandler.init();

        modBus.addListener(ClientInit::onClientInit);
        modBus.addListener(ClientInit::onRegisterRenderers);
        Shaders.init(modBus);
    }

    private static void onClientInit(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(CBMultipartModContent.MULTIPART_BLOCK.get(), e -> true);

        MultipartClientRegistry.register(CBMultipartModContent.WRAPPED_PART.get(), new WrapperPartRenderer());
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        BlockEntityRenderers.register(CBMultipartModContent.MULTIPART_TILE_TYPE.get(), MultipartTileRenderer::new);
    }
}
