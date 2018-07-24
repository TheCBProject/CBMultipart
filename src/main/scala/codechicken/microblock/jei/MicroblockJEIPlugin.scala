package codechicken.microblock.jei

import codechicken.microblock.handler.MicroblockProxy
import codechicken.microblock.{CommonMicroFactory, ItemMicroPart, MicroMaterialRegistry, MicroRecipe}
import mezz.jei.api.{IModPlugin, IModRegistry, ISubtypeRegistry, JEIPlugin}
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack

/**
 * Created by covers1624 on 24/07/18.
 */
@JEIPlugin
class MicroblockJEIPlugin extends IModPlugin {

    override def register(registry: IModRegistry) {
        val blacklist = registry.getJeiHelpers.getIngredientBlacklist
        if (!MicroblockProxy.showAllMicroparts) {

            val stoneId = MicroRecipe.findMaterial(new ItemStack(Blocks.STONE))
            val stoneMatName = if (stoneId != -1) MicroMaterialRegistry.materialName(stoneId) else ""
            for (factoryID <- CommonMicroFactory.factories.indices) {
                val factory = CommonMicroFactory.factories(factoryID)
                if (factory != null) {
                    for (size <- Seq(1, 2, 4)) {
                        MicroMaterialRegistry.getIdMap.filterNot(_._1 == stoneMatName).foreach(e => blacklist.addIngredientToBlacklist(ItemMicroPart.create(factoryID, size, e._1)))
                    }
                }
            }
        }
    }

    override def registerItemSubtypes(subtypeRegistry: ISubtypeRegistry) {
        subtypeRegistry.useNbtForSubtypes(MicroblockProxy.itemMicro)
    }
}
