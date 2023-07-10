package codechicken.microblock.item;

import codechicken.microblock.init.CBMicroblockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.common.TierSortingRegistry;
import org.jetbrains.annotations.Nullable;

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
        if (canBeDepleted()) {
            if (stack.getDamageValue() + 1 >= stack.getMaxDamage()) {
                return ItemStack.EMPTY;
            }
            ItemStack newStack = stack.copy();
            newStack.setDamageValue(stack.getDamageValue() + 1);
            return newStack;
        }
        return stack;
    }

    // Is A greater than or equal to B
    public static boolean isTierGTEQ(Tier a, Tier b) {
        for (Tier tier : TierSortingRegistry.getSortedTiers()) {
            if (tier == b) return true;
            if (tier == a) break;
        }
        return false;
    }

    @Nullable
    public static Tier getSawTier(Item item) {
        if (item instanceof TieredItem t && item.builtInRegistryHolder().is(CBMicroblockTags.Items.TOOL_SAW)) {
            return t.getTier();
        }
        return null;
    }
}
