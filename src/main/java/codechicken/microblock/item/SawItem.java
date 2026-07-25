package codechicken.microblock.item;

import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.init.CBMicroblockTags;
import codechicken.microblock.recipe.MicroRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

/**
 * Item used by {@link MicroRecipe} to cut blocks into microblocks. You can add a custom saw by either:
 * <ol>
 * <li> Attaching {@link SawComponent} to your item as shown below and implementing methods below
 * <li> Extending this class directly
 * </ol>
 * <p>
 * Created by covers1624 on 22/10/22.
 */
public class SawItem extends Item {

    public SawItem(ToolMaterial material, Properties properties) {
        super(material.applyCommonProperties(
                properties.component(CBMicroblockModContent.SAW_COMPONENT, SawComponent.forMaterial(material))
        ));
    }

    @Override
    public ItemStack getCraftingRemainder(ItemStack stack) {
        if (stack.isDamageableItem()) {
            if (stack.getDamageValue() + 1 >= stack.getMaxDamage()) {
                return ItemStack.EMPTY;
            }
            ItemStack newStack = stack.copy();
            newStack.setDamageValue(stack.getDamageValue() + 1);
            return newStack;
        }
        return stack;
    }
}
