package codechicken.microblock.init;

import codechicken.lib.datagen.ItemModelProvider;
import codechicken.lib.datagen.recipe.RecipeProvider;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.Nullable;

import static codechicken.microblock.CBMicroblock.MOD_ID;
import static codechicken.microblock.init.CBMicroblockModContent.*;
import static codechicken.microblock.init.CBMicroblockTags.Items.STONE_ROD;
import static codechicken.microblock.init.CBMicroblockTags.Items.TOOL_SAW;

/**
 * Created by covers1624 on 22/10/22.
 */
public class DataGenerators {

    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    public static void init() {
        LOCK.lock();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(DataGenerators::registerDataGens);
    }

    private static void registerDataGens(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper files = event.getExistingFileHelper();

        gen.addProvider(event.includeClient(), new ItemModels(gen, files));

        gen.addProvider(event.includeServer(), new ItemTagGen(gen, new BlockTagsProvider(gen), files));
        gen.addProvider(event.includeServer(), new Recipes(gen));
    }

    private static class ItemTagGen extends ItemTagsProvider {

        public ItemTagGen(DataGenerator dataGen, BlockTagsProvider blockTags, @Nullable ExistingFileHelper files) {
            super(dataGen, blockTags, MOD_ID, files);
        }

        @Override
        protected void addTags() {
            tag(TOOL_SAW)
                    .add(STONE_SAW.get())
                    .add(IRON_SAW.get())
                    .add(DIAMOND_SAW.get());

            tag(STONE_ROD)
                    .add(STONE_ROD_ITEM.get());
        }
    }

    private static class ItemModels extends ItemModelProvider {

        public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
            super(generator, MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            generated(MICRO_BLOCK_ITEM.get()).noTexture();

            generated(STONE_ROD_ITEM.get());

            generated(STONE_SAW.get());
            generated(IRON_SAW.get());
            generated(DIAMOND_SAW.get());
        }

        @Override
        public String getName() {
            return "CBMicroblock Item Models";
        }
    }

    private static class Recipes extends RecipeProvider {

        public Recipes(DataGenerator generatorIn) {
            super(generatorIn);
        }

        @Override
        protected void registerRecipes() {
            special(MICRO_RECIPE_SERIALIZER.get(),  new ResourceLocation(MOD_ID, "microblock"));

            shapedRecipe(STONE_ROD_ITEM.get())
                    .key('S', Tags.Items.COBBLESTONE)
                    .patternLine("S")
                    .patternLine("S");

            shapedRecipe(STONE_SAW.get())
                    .key('S', Tags.Items.RODS_WOODEN)
                    .key('R', STONE_ROD)
                    .key('M', Items.FLINT)
                    .patternLine("SRR")
                    .patternLine("SMR");

            shapedRecipe(IRON_SAW.get())
                    .key('S', Tags.Items.RODS_WOODEN)
                    .key('R', STONE_ROD)
                    .key('M', Tags.Items.INGOTS_IRON)
                    .patternLine("SRR")
                    .patternLine("SMR");

            shapedRecipe(DIAMOND_SAW.get())
                    .key('S', Tags.Items.RODS_WOODEN)
                    .key('R', STONE_ROD)
                    .key('M', Tags.Items.GEMS_DIAMOND)
                    .patternLine("SRR")
                    .patternLine("SMR");
        }

        @Override
        public String getName() {
            return "CBMicroblock Recipes";
        }
    }
}
