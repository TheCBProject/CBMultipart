package codechicken.multipart.minecraft;

import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.multipart.api.part.render.PartBakedModelRenderer;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Created by covers1624 on 8/11/21.
 */
public class ClientInit {

    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientInit::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        MultipartClientRegistry.register(ModContent.TORCH_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.SOUL_TORCH_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.REDSTONE_TORCH_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.LEVER_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.STONE_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.POLISHED_BLACKSTONE_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.OAK_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.SPRUCE_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.BIRCH_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.JUNGLE_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.ACACIA_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.DARK_OAK_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.CRIMSON_BUTTON_PART.get(), PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.WARPED_BUTTON_PART.get(), PartBakedModelRenderer.simple());
    }
}
