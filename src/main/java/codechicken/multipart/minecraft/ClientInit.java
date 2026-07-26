package codechicken.multipart.minecraft;

import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.RegisterMultipartRenderersEvent;
import codechicken.multipart.api.part.render.BlockStatePartBakedModelRenderer;
import net.neoforged.bus.api.IEventBus;

/**
 * Created by covers1624 on 8/11/21.
 */
public class ClientInit {

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientInit::onRegisterMultipartRenderers);
    }

    private static void onRegisterMultipartRenderers(RegisterMultipartRenderersEvent event) {
        registerRenderers(event, MinecraftMultipartModContent.TORCH_PART.get());
        registerRenderers(event, MinecraftMultipartModContent.SOUL_TORCH_PART.get());
        registerRenderers(event, MinecraftMultipartModContent.REDSTONE_TORCH_PART.get());
        registerRenderers(event, MinecraftMultipartModContent.LEVER_PART.get());
        registerRenderers(event, MinecraftMultipartModContent.STONE_BUTTON_PART.get());
        registerRenderers(event, MinecraftMultipartModContent.POLISHED_BLACKSTONE_BUTTON_PART.get());
        registerRenderers(event, MinecraftMultipartModContent.OAK_BUTTON_PART.get());
        registerRenderers(event, MinecraftMultipartModContent.SPRUCE_BUTTON_PART.get());
        registerRenderers(event, MinecraftMultipartModContent.BIRCH_BUTTON_PART.get());
        registerRenderers(event, MinecraftMultipartModContent.JUNGLE_BUTTON_PART.get());
        registerRenderers(event, MinecraftMultipartModContent.ACACIA_BUTTON_PART.get());
        registerRenderers(event, MinecraftMultipartModContent.DARK_OAK_BUTTON_PART.get());
        registerRenderers(event, MinecraftMultipartModContent.CRIMSON_BUTTON_PART.get());
        registerRenderers(event, MinecraftMultipartModContent.WARPED_BUTTON_PART.get());
    }

    private static void registerRenderers(RegisterMultipartRenderersEvent event, MultipartType<? extends McStatePart> type) {
        event.registerStaticPartRenderer(type, BlockStatePartBakedModelRenderer::new);
        event.registerParticleHandler(type, new McStatePartParticleHandler());
    }
}
