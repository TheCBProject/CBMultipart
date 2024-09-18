package codechicken.multipart.minecraft;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

import static codechicken.multipart.minecraft.MinecraftMultipart.MOD_ID;

@Mod (MOD_ID)
public class MinecraftMultipart {

    public static final String MOD_ID = "cb_multipart_minecraft";

    public MinecraftMultipart(IEventBus modBus) {
        MinecraftMultipartModContent.init(modBus);
        if (FMLEnvironment.dist.isClient()) {
            ClientInit.init(modBus);
        }
    }
}
