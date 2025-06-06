package codechicken.multipart.init;

import codechicken.lib.datagen.ClassModelLoaderBuilder;
import codechicken.multipart.client.MultipartTileBakedModel;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

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
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper files = event.getExistingFileHelper();

        gen.addProvider(event.includeClient(), new BlockStates(output, files));
        gen.addProvider(event.includeServer(), new BlockTags(output, lookupProvider, files));
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

    private static class BlockTags extends BlockTagsProvider {

        public BlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper files) {
            super(output, lookupProvider, MOD_ID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(CBMultipartModContent.ALLOW_MULTIPART_WRAPPING_TAG).add(
                    Blocks.CANDLE,
                    Blocks.WHITE_CANDLE,
                    Blocks.ORANGE_CANDLE,
                    Blocks.MAGENTA_CANDLE,
                    Blocks.LIGHT_BLUE_CANDLE,
                    Blocks.YELLOW_CANDLE,
                    Blocks.LIME_CANDLE,
                    Blocks.PINK_CANDLE,
                    Blocks.GRAY_CANDLE,
                    Blocks.LIGHT_GRAY_CANDLE,
                    Blocks.CYAN_CANDLE,
                    Blocks.PURPLE_CANDLE,
                    Blocks.BLUE_CANDLE,
                    Blocks.BROWN_CANDLE,
                    Blocks.GREEN_CANDLE,
                    Blocks.RED_CANDLE,
                    Blocks.BLACK_CANDLE
            );
            tag(CBMultipartModContent.ALLOW_MULTIPART_WRAPPING_TAG).add(
                    Blocks.FLOWER_POT,
                    Blocks.POTTED_TORCHFLOWER,
                    Blocks.POTTED_OAK_SAPLING,
                    Blocks.POTTED_SPRUCE_SAPLING,
                    Blocks.POTTED_BIRCH_SAPLING,
                    Blocks.POTTED_JUNGLE_SAPLING,
                    Blocks.POTTED_ACACIA_SAPLING,
                    Blocks.POTTED_CHERRY_SAPLING,
                    Blocks.POTTED_DARK_OAK_SAPLING,
                    Blocks.POTTED_MANGROVE_PROPAGULE,
                    Blocks.POTTED_FERN,
                    Blocks.POTTED_DANDELION,
                    Blocks.POTTED_POPPY,
                    Blocks.POTTED_BLUE_ORCHID,
                    Blocks.POTTED_ALLIUM,
                    Blocks.POTTED_AZURE_BLUET,
                    Blocks.POTTED_RED_TULIP,
                    Blocks.POTTED_ORANGE_TULIP,
                    Blocks.POTTED_WHITE_TULIP,
                    Blocks.POTTED_PINK_TULIP,
                    Blocks.POTTED_OXEYE_DAISY,
                    Blocks.POTTED_CORNFLOWER,
                    Blocks.POTTED_LILY_OF_THE_VALLEY,
                    Blocks.POTTED_WITHER_ROSE,
                    Blocks.POTTED_RED_MUSHROOM,
                    Blocks.POTTED_BROWN_MUSHROOM,
                    Blocks.POTTED_DEAD_BUSH,
                    Blocks.POTTED_CACTUS,
                    Blocks.POTTED_CRIMSON_FUNGUS,
                    Blocks.POTTED_WARPED_FUNGUS,
                    Blocks.POTTED_CRIMSON_ROOTS,
                    Blocks.POTTED_WARPED_ROOTS,
                    Blocks.POTTED_AZALEA,
                    Blocks.POTTED_FLOWERING_AZALEA
            );
        }
    }
}
