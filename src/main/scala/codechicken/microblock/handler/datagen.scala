package codechicken.microblock.handler

import java.util.function.Consumer

import codechicken.lib.datagen.ItemModelProvider
import codechicken.microblock.handler.MicroblockMod.modId
import codechicken.microblock.handler.MicroblockModContent._
import net.minecraft.data._
import net.minecraft.item.Items
import net.minecraftforge.client.model.generators.ExistingFileHelper
import net.minecraftforge.common.Tags
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
        gen.addProvider(new ItemTags(gen))
        gen.addProvider(new Recipes(gen))
    }
}

class ItemModels(gen: DataGenerator, helper: ExistingFileHelper) extends ItemModelProvider(gen, modId, helper) {

    override protected def registerModels(): Unit = {
        generated(itemMicroBlock).texture(null)
        generated(itemStoneRod)
        generated(itemStoneSaw)
        generated(itemIronSaw)
        generated(itemDiamondSaw)
    }

    override def getName = "CBMicroblock Item Models"
}

class ItemTags(gen: DataGenerator) extends ItemTagsProvider(gen) {

    override protected def registerTags() {
        getBuilder(stoneRodTag).add(itemStoneRod)
    }

    override def getName = "CBMicroblock Item Tags"
}

class Recipes(gen: DataGenerator) extends RecipeProvider(gen) {


    override protected def registerRecipes(consumer: Consumer[IFinishedRecipe]) {
        CustomRecipeBuilder.customRecipe(microRecipeSerializer).build(consumer, s"$modId:microblock")
        ShapedRecipeBuilder.shapedRecipe(itemStoneRod) //
            .key('S', Tags.Items.COBBLESTONE) //
            .patternLine("S") //
            .patternLine("S") //
            .addCriterion("has_cobble", hasItem(Tags.Items.COBBLESTONE))
            .build(consumer)

        ShapedRecipeBuilder.shapedRecipe(itemStoneSaw)
            .key('S', Tags.Items.RODS_WOODEN)
            .key('R', stoneRodTag)
            .key('M', Items.FLINT)
            .patternLine("SRR")
            .patternLine("SMR")
            .addCriterion("has_sticks", hasItem(Tags.Items.RODS_WOODEN))
            .addCriterion("has_stone_rod", hasItem(stoneRodTag))
            .addCriterion("has_flint", hasItem(Items.FLINT))
            .build(consumer)

        ShapedRecipeBuilder.shapedRecipe(itemIronSaw)
            .key('S', Tags.Items.RODS_WOODEN)
            .key('R', stoneRodTag)
            .key('M', Tags.Items.INGOTS_IRON)
            .patternLine("SRR")
            .patternLine("SMR")
            .addCriterion("has_sticks", hasItem(Tags.Items.RODS_WOODEN))
            .addCriterion("has_stone_rod", hasItem(stoneRodTag))
            .addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON))
            .build(consumer)

        ShapedRecipeBuilder.shapedRecipe(itemDiamondSaw)
            .key('S', Tags.Items.RODS_WOODEN)
            .key('R', stoneRodTag)
            .key('M', Tags.Items.GEMS_DIAMOND)
            .patternLine("SRR")
            .patternLine("SMR")
            .addCriterion("has_sticks", hasItem(Tags.Items.RODS_WOODEN))
            .addCriterion("has_stone_rod", hasItem(stoneRodTag))
            .addCriterion("has_diamond", hasItem(Tags.Items.GEMS_DIAMOND))
            .build(consumer)
    }

    override def getName = "CBMicroblock Recipes"
}
