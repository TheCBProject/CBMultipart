package codechicken.multipart.proxy;

import codechicken.lib.render.block.BlockRenderingRegistry;
import codechicken.lib.util.SneakyUtils;
import codechicken.multipart.client.ClientEventHandler;
import codechicken.multipart.client.MultipartBlockRenderer;
import codechicken.multipart.client.MultipartTileRenderer;
import codechicken.multipart.handler.ControlKeyHandler;
import codechicken.multipart.init.ModContent;
import codechicken.multipart.network.MultipartCPH;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Created by covers1624 on 30/8/20.
 */
public class ProxyClient extends Proxy {

    @Override
    public void clientSetup(FMLClientSetupEvent event) {
        ControlKeyHandler.init();
        ClientEventHandler.init();
        MultipartCPH.init();
        RenderTypeLookup.setRenderLayer(ModContent.blockMultipart, e -> true);
        BlockRenderingRegistry.registerRenderer(new MultipartBlockRenderer());
        ClientRegistry.bindTileEntityRenderer(SneakyUtils.unsafeCast(ModContent.tileMultipartType), MultipartTileRenderer::new);
    }
}
