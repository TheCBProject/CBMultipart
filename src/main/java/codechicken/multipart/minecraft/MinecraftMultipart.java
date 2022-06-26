package codechicken.multipart.minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static codechicken.multipart.minecraft.MinecraftMultipart.MOD_ID;

@Mod (MOD_ID)
public class MinecraftMultipart {

    public static final String MOD_ID = "cb_multipart_minecraft";

    public MinecraftMultipart() {
        FMLJavaModLoadingContext.get().getModEventBus().register(ModContent.class);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientInit::init);
    }
}
