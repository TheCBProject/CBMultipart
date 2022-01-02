package codechicken.multipart.minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.scorge.lang.ScorgeModLoadingContext;

//@Mod (modid = MinecraftMultipartMod.modID, acceptedMinecraftVersions = CodeChickenLib.MC_VERSION_DEP)
public class MinecraftMultipart {

    public static final String MOD_ID = "cb_multipart_minecraft";

    public MinecraftMultipart() {
        ScorgeModLoadingContext.get().getModEventBus().register(ModContent.class);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientInit::init);
    }
}
