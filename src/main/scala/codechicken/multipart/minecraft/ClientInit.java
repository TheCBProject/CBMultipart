package codechicken.multipart.minecraft;

import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.multipart.api.part.render.PartBakedModelRenderer;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.scorge.lang.ScorgeModLoadingContext;

/**
 * Created by covers1624 on 8/11/21.
 */
public class ClientInit {

    public static void init() {
        ScorgeModLoadingContext.get().getModEventBus().addListener(ClientInit::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        MultipartClientRegistry.register(ModContent.torchPartType, PartBakedModelRenderer.simple(TorchPart.class));
        MultipartClientRegistry.register(ModContent.redstoneTorchPartType, PartBakedModelRenderer.simple(RedstoneTorchPart.class));
        MultipartClientRegistry.register(ModContent.leverPartType, PartBakedModelRenderer.simple(LeverPart.class));
        MultipartClientRegistry.register(ModContent.stoneButtonPartType, PartBakedModelRenderer.simple(ButtonPart.StoneButtonPart.class));
        MultipartClientRegistry.register(ModContent.oakButtonPartType, PartBakedModelRenderer.simple(ButtonPart.OakButtonPart.class));
        MultipartClientRegistry.register(ModContent.spruceButtonPartType, PartBakedModelRenderer.simple(ButtonPart.SpruceButtonPart.class));
        MultipartClientRegistry.register(ModContent.birchButtonPartType, PartBakedModelRenderer.simple(ButtonPart.BirchButtonPart.class));
        MultipartClientRegistry.register(ModContent.jungleButtonPartType, PartBakedModelRenderer.simple(ButtonPart.JungleButtonPart.class));
        MultipartClientRegistry.register(ModContent.acaciaButtonPartType, PartBakedModelRenderer.simple(ButtonPart.AcaciaButtonPart.class));
        MultipartClientRegistry.register(ModContent.darkOakButtonPartType, PartBakedModelRenderer.simple(ButtonPart.DarkOakButtonPart.class));
    }
}
