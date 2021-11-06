package codechicken.microblock

import codechicken.lib.config.ConfigTag
import net.minecraft.item.{Item, ItemGroup, ItemStack}

/**
 * Interface for items that are 'saws'
 */
trait Saw extends Item {
    /**
     * The maximum harvest level that some version of this saw is capable of cutting
     */
    def getMaxCuttingStrength: Int = getCuttingStrength(new ItemStack(this))

    /**
     * The harvest level this saw is capable of cutting
     */
    def getCuttingStrength(item: ItemStack): Int
}

class ItemSaw(sawTag: ConfigTag, val harvestLevel: Int) extends Item({
    val durability = sawTag.getTag("durability").setDefaultInt(1 << harvestLevel + 8).getInt
    val prop = new Item.Properties()
        .tab(ItemGroup.TAB_TOOLS)
        .setNoRepair()
    if (durability > 0) {
        prop.durability(durability)
    }
    prop
}) with Saw {

    override def hasContainerItem(stack: ItemStack) = true

    override def getContainerItem(stack: ItemStack):ItemStack =
        if (canBeDepleted) {
            if (stack.getDamageValue + 1 >= stack.getMaxDamage) {
                return ItemStack.EMPTY
            }
            val newStack = stack.copy()
            newStack.setDamageValue(stack.getDamageValue + 1)
            newStack
        } else {
            stack
        }

    def getCuttingStrength(item: ItemStack) = harvestLevel
}
