package codechicken.multipart.proxy;

import codechicken.lib.render.block.BlockRenderingRegistry;
import codechicken.multipart.client.ClientEventHandler;
import codechicken.multipart.client.MultipartBlockRenderer;
import codechicken.multipart.handler.ControlKeyHandler;
import codechicken.multipart.init.CBMultipartModContent;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Created by covers1624 on 30/8/20.
 */
public class ProxyClient extends Proxy {

    @Override
    public void clientSetup(FMLClientSetupEvent event) {
        ControlKeyHandler.init();
        ClientEventHandler.init();
        ItemBlockRenderTypes.setRenderLayer(CBMultipartModContent.blockMultipart, e -> true);
        BlockRenderingRegistry.registerRenderer(CBMultipartModContent.blockMultipart, new MultipartBlockRenderer());
//        ClientRegistry.bindTileEntityRenderer(SneakyUtils.unsafeCast(CBMultipartModContent.tileMultipartType), MultipartTileRenderer::new);
    }
}
