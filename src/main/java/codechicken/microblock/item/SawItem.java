package codechicken.microblock.item;

import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.init.CBMicroblockTags;
import codechicken.microblock.recipe.MicroRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;

/**
 * Item used by {@link MicroRecipe} to cut blocks into microblocks. You can add a custom saw by either:
 * <ol>
 * <li> Attaching {@link SawComponent} to your item as shown below and implementing methods below
 * <li> Extending this class directly
 * <li> Tagging your item with {@link CBMicroblockTags.Items#TOOL_SAW} and implementing methods
 *      below (legacy method without rule-based cutting).
 * </ol>
 * <p>
 * Created by covers1624 on 22/10/22.
 */
public class SawItem extends TieredItem {

    public SawItem(Tier tier, Properties properties) {
        super(tier, properties.component(CBMicroblockModContent.SAW_COMPONENT, SawComponent.forTier(tier)));
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
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
