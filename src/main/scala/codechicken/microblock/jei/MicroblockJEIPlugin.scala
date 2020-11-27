package codechicken.microblock.jei

import codechicken.microblock.handler.{MicroblockMod, MicroblockModContent, MicroblockProxy}
import codechicken.microblock.jei.MicroblockJEIPlugin.instance
import codechicken.microblock.{CommonMicroFactory, ItemMicroBlock, MicroMaterialRegistry, MicroRecipe}
import mezz.jei.api.constants.VanillaTypes
import mezz.jei.api.registration.ISubtypeRegistration
import mezz.jei.api.runtime.IJeiRuntime
import mezz.jei.api.{IModPlugin, JeiPlugin}
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation

import scala.jdk.CollectionConverters._

/**
 * Created by covers1624 on 24/07/18.
 */
@JeiPlugin
class MicroblockJEIPlugin extends IModPlugin {

    private lazy val blacklist: Seq[ItemStack] = computeBlacklist

    if (instance == null) {
        instance = this
    }

    override def getPluginUid: ResourceLocation = new ResourceLocation(MicroblockMod.modId, "microblocks")

    override def onRuntimeAvailable(runtime: IJeiRuntime) {
        if (instance != this) return

        val ingredientManager = runtime.getIngredientManager
        if (!MicroblockProxy.showAllMicroparts) {
            ingredientManager.removeIngredientsAtRuntime(VanillaTypes.ITEM, blacklist.asJavaCollection)
        }
    }

    override def registerItemSubtypes(subtypeRegistry: ISubtypeRegistration) {
        if (instance != this) return

        subtypeRegistry.useNbtForSubtypes(MicroblockModContent.itemMicroBlock)
    }

    private def computeBlacklist: Seq[ItemStack] = {
        var blacklist = Seq[ItemStack]()
        val stoneId = MicroRecipe.findMaterial(new ItemStack(Blocks.STONE))
        val stoneMatName = if (stoneId != -1) MicroMaterialRegistry.getMaterialName(stoneId) else null
        for (factoryID <- CommonMicroFactory.factories.indices) {
            val factory = CommonMicroFactory.factories(factoryID)
            if (factory != null) {
                for (size <- Seq(1, 2, 4)) {
                    blacklist ++= MicroMaterialRegistry.MICRO_MATERIALS.getKeys.asScala
                        .filterNot(_ == stoneMatName)
                        .map(e => ItemMicroBlock.create(factoryID, size, e))

                }
            }
        }
        blacklist
    }
}

object MicroblockJEIPlugin {
    private var instance: MicroblockJEIPlugin = _
}
