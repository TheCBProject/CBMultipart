package codechicken.microblock.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;

/**
 * Created by covers1624 on 22/10/22.
 */
public class SawItem extends TieredItem {

    public SawItem(Tier tier, Properties properties) {
        super(tier, properties);
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
