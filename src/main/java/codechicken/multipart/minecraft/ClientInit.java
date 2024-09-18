package codechicken.multipart.minecraft;

import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.multipart.api.part.render.PartBakedModelRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Created by covers1624 on 8/11/21.
 */
public class ClientInit {

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientInit::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        MultipartClientRegistry.register(MinecraftMultipartModContent.TORCH_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(MinecraftMultipartModContent.SOUL_TORCH_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(MinecraftMultipartModContent.REDSTONE_TORCH_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(MinecraftMultipartModContent.LEVER_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(MinecraftMultipartModContent.STONE_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(MinecraftMultipartModContent.POLISHED_BLACKSTONE_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(MinecraftMultipartModContent.OAK_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(MinecraftMultipartModContent.SPRUCE_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(MinecraftMultipartModContent.BIRCH_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(MinecraftMultipartModContent.JUNGLE_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(MinecraftMultipartModContent.ACACIA_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(MinecraftMultipartModContent.DARK_OAK_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(MinecraftMultipartModContent.CRIMSON_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(MinecraftMultipartModContent.WARPED_BUTTON_PART.get(), PartBakedModelRenderer.simple());
    }
}
