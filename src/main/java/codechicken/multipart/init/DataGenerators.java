package codechicken.multipart.init;

import codechicken.lib.datagen.ClassModelLoaderBuilder;
import codechicken.multipart.client.MultipartTileBakedModel;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import static codechicken.multipart.CBMultipart.MOD_ID;

/**
 * Created by covers1624 on 21/3/20.
 */
public class DataGenerators {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    public static void init(IEventBus modBus) {
        LOCK.lock();
        modBus.addListener(DataGenerators::gatherDataGenerators);
    }

    public static void gatherDataGenerators(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        ExistingFileHelper files = event.getExistingFileHelper();

        gen.addProvider(event.includeClient(), new BlockStates(output, files));
    }

    private static class BlockStates extends BlockStateProvider {

        public BlockStates(PackOutput output, ExistingFileHelper exFileHelper) {
            super(output, MOD_ID, exFileHelper);
        }

        @Override
        protected void registerStatesAndModels() {
            ModelFile model = models()
                    .withExistingParent("multipart", "block")
                    .customLoader(ClassModelLoaderBuilder::new)
                    .clazz(MultipartTileBakedModel.class)
                    .end();
            simpleBlock(CBMultipartModContent.MULTIPART_BLOCK.get(), model);
        }
    }
}
