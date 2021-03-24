package codechicken.multipart.init;

import codechicken.lib.util.CrashLock;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import javax.annotation.Nonnull;

import static codechicken.multipart.CBMultipart.MOD_ID;

/**
 * Created by covers1624 on 21/3/20.
 */
public class DataGenerators {

    private static CrashLock LOCK = new CrashLock("Already initialized.");

    public static void init(IEventBus eventBus) {
        LOCK.lock();
        eventBus.addListener(DataGenerators::gatherDataGenerators);
    }

    public static void gatherDataGenerators(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper files = event.getExistingFileHelper();
        if (event.includeClient()) {
            gen.addProvider(new BlockStates(gen, files));
        }
    }

    private static class BlockStates extends BlockStateProvider {

        public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
            super(gen, MOD_ID, exFileHelper);
        }

        @Nonnull
        @Override
        public String getName() {
            return "CBMultipart BlockStates";
        }

        @Override
        protected void registerStatesAndModels() {
            ModelFile model = models()//
                    .withExistingParent("dummy", "block");
            simpleBlock(CBMultipartModContent.blockMultipart, model);
        }
    }
}
