package codechicken.multipart.init;

import codechicken.lib.render.block.BlockRenderingRegistry;
import codechicken.multipart.client.ClientEventHandler;
import codechicken.multipart.client.MultipartBlockRenderer;
import codechicken.multipart.client.MultipartTileRenderer;
import codechicken.multipart.handler.ControlKeyHandler;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Created by covers1624 on 26/6/22.
 */
public class ClientInit {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    public void init() {
        LOCK.lock();

        ControlKeyHandler.init();
        ClientEventHandler.init();
        ItemBlockRenderTypes.setRenderLayer(CBMultipartModContent.blockMultipart, e -> true);
        BlockRenderingRegistry.registerRenderer(CBMultipartModContent.blockMultipart, new MultipartBlockRenderer());

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(ClientInit::onRegisterRenderers);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        BlockEntityRenderers.register(CBMultipartModContent.tileMultipartType, MultipartTileRenderer::new);
    }
}
