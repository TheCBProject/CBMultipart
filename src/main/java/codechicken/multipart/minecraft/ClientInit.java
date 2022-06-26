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
        MultipartClientRegistry.register(ModContent.torchPartType, PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.redstoneTorchPartType, PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.leverPartType, PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.stoneButtonPartType, PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.oakButtonPartType, PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.spruceButtonPartType, PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.birchButtonPartType, PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.jungleButtonPartType, PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.acaciaButtonPartType, PartBakedModelRenderer.simple());
        MultipartClientRegistry.register(ModContent.darkOakButtonPartType, PartBakedModelRenderer.simple());
    }
}
