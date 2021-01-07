package codechicken.multipart.trait;

import codechicken.lib.util.ArrayUtils;
import codechicken.multipart.api.annotation.MultiPartTrait;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.TileMultiPart;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by covers1624 on 1/1/21.
 */
@MultiPartTrait (IInventory.class)
@MultiPartTrait (ISidedInventory.class)
public class TInventoryTile extends TileMultiPart implements ISidedInventory {

    private List<IInventory> invList = new ArrayList<>();

    private int sizeSum = 0;
    private int[] invSize = new int[0];
    private int[][] faceSlots = ArrayUtils.fill(new int[6][0], null);

    @Override
    public void copyFrom(TileMultiPart that) {
        super.copyFrom(that);
        if (that instanceof TInventoryTile) {
            invList = ((TInventoryTile) that).invList;
            rebuildSlotMap();
        }
    }

    @Override
    public void bindPart(TMultiPart part) {
        super.bindPart(part);
        if (part instanceof IInventory) {
            invList.add((IInventory) part);
            rebuildSlotMap();
        }
    }

    @Override
    public void partRemoved(TMultiPart part, int p) {
        super.partRemoved(part, p);
        if (part instanceof IInventory) {
            invList.remove(part);
            rebuildSlotMap();
        }
    }

    @Override
    public void clearParts() {
        super.clearParts();
        invList.clear();

        sizeSum = 0;
        invSize = new int[0];
        faceSlots = ArrayUtils.fill(new int[6][0], null);
    }

    private void rebuildSlotMap() {
        sizeSum = invList.stream().mapToInt(IInventory::getSizeInventory).sum();
        invSize = invList.stream().mapToInt(IInventory::getSizeInventory).toArray();
        faceSlots = ArrayUtils.fill(new int[6][0], null);
    }

    @Override
    public void clear() {
        for (IInventory inv : invList) {
            inv.clear();
        }
    }

    @Override
    public int getSizeInventory() {
        return sizeSum;
    }

    @Override
    public boolean isEmpty() {
        for (IInventory inv : invList) {
            if (!inv.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        int i = 0;
        while (slot >= invSize[i]) {
            slot -= invSize[i];
            i++;
        }
        return invList.get(i).getStackInSlot(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        int i = 0;
        while (slot >= invSize[i]) {
            slot -= invSize[i];
            i++;
        }
        return invList.get(i).decrStackSize(slot, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        int i = 0;
        while (slot >= invSize[i]) {
            slot -= invSize[i];
            i++;
        }
        return invList.get(i).removeStackFromSlot(slot);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        int i = 0;
        while (slot >= invSize[i]) {
            slot -= invSize[i];
            i++;
        }
        invList.get(i).setInventorySlotContents(slot, stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] slots = faceSlots[side.ordinal()];
        if (slots == null) {
            IntList intList = new IntArrayList();
            int base = 0;
            for (IInventory inv : invList) {
                if (inv instanceof ISidedInventory) {
                    int finalBase = base;
                    Arrays.stream(((ISidedInventory) inv).getSlotsForFace(side))
                            .map(j -> j + finalBase)
                            .forEach(intList::add);
                }
                base += inv.getSizeInventory();
            }
            slots = intList.toIntArray();
            faceSlots[side.ordinal()] = slots;
        }
        return slots;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, @Nullable Direction side) {
        int i = 0;
        while (slot >= invSize[i]) {
            slot -= invSize[i];
            i++;
        }
        IInventory inv = invList.get(i);
        if (inv instanceof ISidedInventory) {
            return ((ISidedInventory) inv).canInsertItem(slot, stack, side);
        }
        return true;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, Direction side) {
        int i = 0;
        while (slot >= invSize[i]) {
            slot -= invSize[i];
            i++;
        }
        IInventory inv = invList.get(i);
        if (inv instanceof ISidedInventory) {
            return ((ISidedInventory) inv).canExtractItem(slot, stack, side);
        }
        return true;
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return true;
    }
}
