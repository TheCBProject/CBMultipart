package codechicken.multipart.minecraft;

import net.minecraftforge.scorge.lang.ScorgeModLoadingContext;

//@Mod (modid = MinecraftMultipartMod.modID, acceptedMinecraftVersions = CodeChickenLib.MC_VERSION_DEP)
public class MinecraftMultipart {

    public static final String MOD_ID = "cb_multipart_minecraft";

    public MinecraftMultipart() {
        ScorgeModLoadingContext.get().getModEventBus().register(ModContent.class);
    }

    //    @Mod.EventHandler
    //    public void preInit(FMLPreInitializationEvent event) {
    //        new Content().init();
    //        PacketCustom.assignHandler(McMultipartSPH.channel, new McMultipartSPH());
    //        if (FMLCommonHandler.instance().getSide().isClient()) {
    //            PacketCustom.assignHandler(McMultipartCPH.channel, new McMultipartCPH());
    //        }
    //    }
}
