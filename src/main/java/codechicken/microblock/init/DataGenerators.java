package codechicken.microblock.init;

import codechicken.lib.datagen.ItemModelProvider;
import codechicken.lib.datagen.recipe.RecipeProvider;
import codechicken.microblock.client.MicroblockItemRenderer;
import codechicken.microblock.recipe.MicroRecipe;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static codechicken.microblock.CBMicroblock.MOD_ID;
import static codechicken.microblock.init.CBMicroblockModContent.*;
import static codechicken.microblock.init.CBMicroblockTags.Items.STONE_ROD;
import static codechicken.microblock.init.CBMicroblockTags.Items.TOOL_SAW;

/**
 * Created by covers1624 on 22/10/22.
 */
public class DataGenerators {

    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    public static void init(IEventBus modBus) {
        LOCK.lock();

        modBus.addListener(DataGenerators::registerDataGens);
    }

    private static void registerDataGens(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        ExistingFileHelper files = event.getExistingFileHelper();

        gen.addProvider(event.includeClient(), new ItemModels(output, files));
        gen.addProvider(event.includeServer(), new ItemTagGen(output, event.getLookupProvider(), CompletableFuture.supplyAsync(TagsProvider.TagLookup::empty), files));
        gen.addProvider(event.includeServer(), new Recipes(event.getLookupProvider(), output));
    }

    private static class ItemTagGen extends ItemTagsProvider {

        public ItemTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> providerLookup, CompletableFuture<TagsProvider.TagLookup<Block>> tagLookup, @Nullable ExistingFileHelper files) {
            super(output, providerLookup, tagLookup, MOD_ID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            tag(TOOL_SAW)
                    .add(STONE_SAW.get())
                    .add(IRON_SAW.get())
                    .add(DIAMOND_SAW.get());

            tag(STONE_ROD)
                    .add(STONE_ROD_ITEM.get());
        }
    }

    private static class ItemModels extends ItemModelProvider {

        public ItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            clazz(MICRO_BLOCK_ITEM.get(), MicroblockItemRenderer.class);

            generated(STONE_ROD_ITEM.get());

            generated(STONE_SAW.get());
            generated(IRON_SAW.get());
            generated(DIAMOND_SAW.get());
        }
    }

    private static class Recipes extends RecipeProvider {

        public Recipes(CompletableFuture<HolderLookup.Provider> registries, PackOutput output) {
            super(registries, output, MOD_ID);
        }

        @Override
        protected void registerRecipes() {
            special(ResourceLocation.fromNamespaceAndPath(MOD_ID, "microblock"), MicroRecipe::new);

            shapedRecipe(STONE_ROD_ITEM.get())
                    .key('S', Tags.Items.COBBLESTONES)
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
    }
}
