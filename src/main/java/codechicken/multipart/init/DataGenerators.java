package codechicken.multipart.init;

import codechicken.multipart.client.MultipartTileBakedModel;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import static codechicken.multipart.CBMultipart.MOD_ID;
import static codechicken.multipart.init.CBMultipartModContent.MULTIPART_BLOCK;

/**
 * Created by covers1624 on 21/3/20.
 */
public class DataGenerators {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    public static void init(IEventBus modBus) {
        LOCK.lock();
        modBus.addListener(DataGenerators::gatherDataGenerators);
    }

    public static void gatherDataGenerators(GatherDataEvent.Client event) {
        event.createProvider(Models::new);
    }

    private static class Models extends ModelProvider {

        public Models(PackOutput output) {
            super(output, MOD_ID);
        }

        @Override
        protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
            blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
                    MULTIPART_BLOCK.get(),
                    MultiVariant.of(new CustomBlockStateModelBuilder.Simple(new MultipartTileBakedModel.Unbaked()))
            ));
        }
    }
}
