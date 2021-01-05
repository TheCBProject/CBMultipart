package codechicken.multipart.capability;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by covers1624 on 4/1/21.
 */
public class MergedItemHandler implements IItemHandler {

    protected final IItemHandler[] invMap;
    protected final int[] slotMap;

    public MergedItemHandler(List<IItemHandler> handlers) {
        int sum = handlers.stream().mapToInt(IItemHandler::getSlots).sum();
        invMap = new IItemHandler[sum];
        slotMap = new int[sum];

        int i = 0;
        for (IItemHandler inv : handlers) {
            for (int slot = 0; slot < inv.getSlots(); slot++) {
                invMap[i] = inv;
                slotMap[i] = slot;
                i++;
            }
        }
    }

    public static IItemHandler merge(List<IItemHandler> handlers) {
        if (handlers.stream().allMatch(e -> e instanceof IItemHandlerModifiable)) {
            return new MergedModifiableItemHandler(handlers);
        }
        return new MergedItemHandler(handlers);
    }

    @Override
    public int getSlots() {
        return slotMap.length;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return invMap[slot].getStackInSlot(slotMap[slot]);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return invMap[slot].insertItem(slotMap[slot], stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return invMap[slot].extractItem(slotMap[slot], amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return invMap[slot].getSlotLimit(slotMap[slot]);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return invMap[slot].isItemValid(slotMap[slot], stack);
    }

    public static class MergedModifiableItemHandler extends MergedItemHandler implements IItemHandlerModifiable {

        private MergedModifiableItemHandler(List<IItemHandler> handlers) {
            super(handlers);
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            ((IItemHandlerModifiable) invMap[slot]).setStackInSlot(slot, stack);
        }
    }
}
