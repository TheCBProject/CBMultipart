package codechicken.microblock;

import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.init.ClientInit;
import codechicken.microblock.init.DataGenerators;
import codechicken.microblock.util.MicroMaterialRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

import static codechicken.microblock.CBMicroblock.MOD_ID;

/**
 * Created by covers1624 on 26/6/22.
 */
@Mod (MOD_ID)
public class CBMicroblock {

    public static final String MOD_ID = "cb_microblock";

    public CBMicroblock(IEventBus modBus) {
        MicroMaterialRegistry.init(modBus);
        CBMicroblockModContent.init(modBus);

        DataGenerators.init(modBus);

        if (FMLEnvironment.dist.isClient()) {
            ClientInit.init(modBus);
        }
    }
}
