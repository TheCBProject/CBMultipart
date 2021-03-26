package codechicken.microblock.handler

import codechicken.lib.datagen.ItemModelProvider
import codechicken.lib.datagen.recipe.RecipeProvider
import codechicken.microblock.handler.MicroblockMod.modId
import codechicken.microblock.handler.MicroblockModContent._
import net.minecraft.data.{BlockTagsProvider, DataGenerator, ItemTagsProvider}
import net.minecraft.item.Items
import net.minecraftforge.common.Tags
import net.minecraftforge.common.data.ExistingFileHelper
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent

/**
 * Created by covers1624 on 15/10/20.
 */
object DataGenerators {

    @SubscribeEvent
    def gatherDataGenerators(event: GatherDataEvent) {
        val gen = event.getGenerator
        val helper = event.getExistingFileHelper
        if (event.includeClient()) {
            gen.addProvider(new ItemModels(gen, helper))
        }
        if (event.includeServer()) {
            gen.addProvider(new ItemTags(gen, helper))
            gen.addProvider(new Recipes(gen))
        }
    }
}

class ItemModels(gen: DataGenerator, helper: ExistingFileHelper) extends ItemModelProvider(gen, modId, helper) {

    override def getName = "CBMicroblock Item Models"

    override protected def registerModels(): Unit = {
        generated(itemMicroBlock).texture(null)
        generated(itemStoneRod)
        generated(itemStoneSaw)
        generated(itemIronSaw)
        generated(itemDiamondSaw)
    }
}

class ItemTags(gen: DataGenerator, helper: ExistingFileHelper) extends ItemTagsProvider(gen, new BlockTagsProvider(gen, modId, helper), modId, helper) {

    override def getName = "CBMicroblock Item Tags"

    override protected def addTags() {
        tag(stoneRodTag).add(itemStoneRod)
    }
}

class Recipes(gen: DataGenerator) extends RecipeProvider(gen) {

    override def getName = "CBMicroblock Recipes"

    override protected def registerRecipes() {
        special(microRecipeSerializer, s"$modId:microblock")
        shapedRecipe(itemStoneRod)
            .key('S', Tags.Items.COBBLESTONE)
            .patternLine("S")
            .patternLine("S")

        shapedRecipe(itemStoneSaw)
            .key('S', Tags.Items.RODS_WOODEN)
            .key('R', stoneRodTag)
            .key('M', Items.FLINT)
            .patternLine("SRR")
            .patternLine("SMR")

        shapedRecipe(itemIronSaw)
            .key('S', Tags.Items.RODS_WOODEN)
            .key('R', stoneRodTag)
            .key('M', Tags.Items.INGOTS_IRON)
            .patternLine("SRR")
            .patternLine("SMR")

        shapedRecipe(itemDiamondSaw)
            .key('S', Tags.Items.RODS_WOODEN)
            .key('R', stoneRodTag)
            .key('M', Tags.Items.GEMS_DIAMOND)
            .patternLine("SRR")
            .patternLine("SMR")
    }
}
