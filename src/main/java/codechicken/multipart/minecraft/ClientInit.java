package codechicken.multipart.minecraft;

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
        event.registerStaticPartRenderer(MinecraftMultipartModContent.TORCH_PART.get(), BlockStatePartBakedModelRenderer::new);
        event.registerStaticPartRenderer(MinecraftMultipartModContent.SOUL_TORCH_PART.get(), BlockStatePartBakedModelRenderer::new);
        event.registerStaticPartRenderer(MinecraftMultipartModContent.REDSTONE_TORCH_PART.get(), BlockStatePartBakedModelRenderer::new);
        event.registerStaticPartRenderer(MinecraftMultipartModContent.LEVER_PART.get(), BlockStatePartBakedModelRenderer::new);
        event.registerStaticPartRenderer(MinecraftMultipartModContent.STONE_BUTTON_PART.get(), BlockStatePartBakedModelRenderer::new);
        event.registerStaticPartRenderer(MinecraftMultipartModContent.POLISHED_BLACKSTONE_BUTTON_PART.get(), BlockStatePartBakedModelRenderer::new);
        event.registerStaticPartRenderer(MinecraftMultipartModContent.OAK_BUTTON_PART.get(), BlockStatePartBakedModelRenderer::new);
        event.registerStaticPartRenderer(MinecraftMultipartModContent.SPRUCE_BUTTON_PART.get(), BlockStatePartBakedModelRenderer::new);
        event.registerStaticPartRenderer(MinecraftMultipartModContent.BIRCH_BUTTON_PART.get(), BlockStatePartBakedModelRenderer::new);
        event.registerStaticPartRenderer(MinecraftMultipartModContent.JUNGLE_BUTTON_PART.get(), BlockStatePartBakedModelRenderer::new);
        event.registerStaticPartRenderer(MinecraftMultipartModContent.ACACIA_BUTTON_PART.get(), BlockStatePartBakedModelRenderer::new);
        event.registerStaticPartRenderer(MinecraftMultipartModContent.DARK_OAK_BUTTON_PART.get(), BlockStatePartBakedModelRenderer::new);
        event.registerStaticPartRenderer(MinecraftMultipartModContent.CRIMSON_BUTTON_PART.get(), BlockStatePartBakedModelRenderer::new);
        event.registerStaticPartRenderer(MinecraftMultipartModContent.WARPED_BUTTON_PART.get(), BlockStatePartBakedModelRenderer::new);
    }
}
