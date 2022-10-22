package codechicken.microblock;

import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.init.ClientInit;
import codechicken.microblock.init.DataGenerators;
import codechicken.microblock.util.MicroMaterialRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import static codechicken.microblock.CBMicroblock.MOD_ID;

/**
 * Created by covers1624 on 26/6/22.
 */
@Mod (MOD_ID)
public class CBMicroblock {

    public static final String MOD_ID = "cb_microblock";

    public CBMicroblock() {
        MicroMaterialRegistry.init();
        CBMicroblockModContent.init();

        DataGenerators.init();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientInit::init);
    }
}
