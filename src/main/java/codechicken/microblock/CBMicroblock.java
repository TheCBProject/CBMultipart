package codechicken.microblock;

import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.init.ClientInit;
import codechicken.microblock.util.MicroMaterialRegistries;
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
        MicroMaterialRegistries.init();
        CBMicroblockModContent.init();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientInit::init);
    }
}
