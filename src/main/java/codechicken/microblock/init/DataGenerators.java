package codechicken.microblock.init;

import codechicken.lib.datagen.recipe.RecipeProvider;
import codechicken.microblock.client.MicroblockItemRenderer;
import codechicken.microblock.recipe.MicroRecipe;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

import static codechicken.microblock.CBMicroblock.MOD_ID;
import static codechicken.microblock.init.CBMicroblockModContent.*;
import static codechicken.microblock.init.CBMicroblockTags.Items.STONE_ROD;

/**
 * Created by covers1624 on 22/10/22.
 */
public class DataGenerators {

    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    public static void init(IEventBus modBus) {
        LOCK.lock();

        modBus.addListener(DataGenerators::registerDataGens);
    }

    private static void registerDataGens(GatherDataEvent.Client event) {
        event.createProvider(Models::new);
        event.createProvider(ItemTags::new);
        event.createProvider(Recipes::new);
    }

    private static class Models extends ModelProvider {

        public Models(PackOutput output) {
            super(output, MOD_ID);
        }

        @Override
        protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
            itemModels.itemModelOutput.accept(
                    MICRO_BLOCK_ITEM.get(),
                    ItemModelUtils.specialModel(Identifier.withDefaultNamespace("block/block"), new MicroblockItemRenderer.Unbaked())
            );

            itemModels.generateFlatItem(STONE_ROD_ITEM.get(), ModelTemplates.FLAT_ITEM);
            itemModels.generateFlatItem(STONE_SAW.get(), ModelTemplates.FLAT_ITEM);
            itemModels.generateFlatItem(IRON_SAW.get(), ModelTemplates.FLAT_ITEM);
            itemModels.generateFlatItem(GOLD_SAW.get(), ModelTemplates.FLAT_ITEM);
            itemModels.generateFlatItem(DIAMOND_SAW.get(), ModelTemplates.FLAT_ITEM);
            itemModels.generateFlatItem(NETHERITE_SAW.get(), ModelTemplates.FLAT_ITEM);
        }
    }

    private static class ItemTags extends ItemTagsProvider {

        public ItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> providerLookup) {
            super(output, providerLookup, MOD_ID);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            tag(STONE_ROD)
                    .add(STONE_ROD_ITEM.get());
        }
    }

    private static class Recipes extends RecipeProvider {

        public Recipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries, MOD_ID);
        }

        @Override
        protected void registerRecipes() {
            special(Identifier.fromNamespaceAndPath(MOD_ID, "microblock"), MicroRecipe::new);

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

            shapedRecipe(GOLD_SAW.get())
                    .key('S', Tags.Items.RODS_WOODEN)
                    .key('R', STONE_ROD)
                    .key('M', Tags.Items.INGOTS_GOLD)
                    .patternLine("SRR")
                    .patternLine("SMR");

            shapedRecipe(DIAMOND_SAW.get())
                    .key('S', Tags.Items.RODS_WOODEN)
                    .key('R', STONE_ROD)
                    .key('M', Tags.Items.GEMS_DIAMOND)
                    .patternLine("SRR")
                    .patternLine("SMR");

            shapedRecipe(NETHERITE_SAW.get())
                    .key('S', Tags.Items.RODS_WOODEN)
                    .key('R', STONE_ROD)
                    .key('M', Tags.Items.INGOTS_NETHERITE)
                    .patternLine("SRR")
                    .patternLine("SMR");
        }
    }
}
