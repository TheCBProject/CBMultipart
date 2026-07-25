package codechicken.multipart.init;

import codechicken.multipart.block.BlockMultipartClientExtensions;
import codechicken.multipart.client.ClientEventHandler;
import codechicken.multipart.client.MultipartTileBakedModel;
import codechicken.multipart.client.MultipartTileRenderer;
import codechicken.multipart.handler.ControlKeyHandler;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * Created by covers1624 on 26/6/22.
 */
public class ClientInit {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    public static void init(IEventBus modBus) {
        LOCK.lock();

        ControlKeyHandler.init(modBus);
        ClientEventHandler.init();

        modBus.addListener(ClientInit::onRegisterRenderers);
        modBus.addListener(ClientInit::onRegisterBlockStateModels);
        modBus.addListener(ClientInit::onRegisterClientExtensions);
    }

    private static void onRegisterBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(MultipartTileBakedModel.Unbaked.TYPE, MultipartTileBakedModel.Unbaked.CODEC);
    }

    private static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerBlock(new BlockMultipartClientExtensions(), CBMultipartModContent.MULTIPART_BLOCK.get());
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        BlockEntityRenderers.register(CBMultipartModContent.MULTIPART_TILE_TYPE.get(), MultipartTileRenderer::new);
    }
}
