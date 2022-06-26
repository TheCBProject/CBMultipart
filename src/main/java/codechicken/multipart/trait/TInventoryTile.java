package codechicken.multipart.trait;

import codechicken.lib.util.ArrayUtils;
import codechicken.multipart.api.annotation.MultiPartTrait;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.TileMultiPart;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by covers1624 on 1/1/21.
 */
@MultiPartTrait (Container.class)
@MultiPartTrait (WorldlyContainer.class)
public class TInventoryTile extends TileMultiPart implements WorldlyContainer {

    private List<Container> invList = new ArrayList<>();

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
        if (part instanceof Container) {
            invList.add((Container) part);
            rebuildSlotMap();
        }
    }

    @Override
    public void partRemoved(TMultiPart part, int p) {
        super.partRemoved(part, p);
        if (part instanceof Container) {
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
        sizeSum = invList.stream().mapToInt(Container::getContainerSize).sum();
        invSize = invList.stream().mapToInt(Container::getContainerSize).toArray();
        faceSlots = ArrayUtils.fill(new int[6][0], null);
    }

    @Override
    public void clearContent() {
        for (Container inv : invList) {
            inv.clearContent();
        }
    }

    @Override
    public int getContainerSize() {
        return sizeSum;
    }

    @Override
    public boolean isEmpty() {
        for (Container inv : invList) {
            if (!inv.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        int i = 0;
        while (slot >= invSize[i]) {
            slot -= invSize[i];
            i++;
        }
        return invList.get(i).getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        int i = 0;
        while (slot >= invSize[i]) {
            slot -= invSize[i];
            i++;
        }
        return invList.get(i).removeItem(slot, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        int i = 0;
        while (slot >= invSize[i]) {
            slot -= invSize[i];
            i++;
        }
        return invList.get(i).removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        int i = 0;
        while (slot >= invSize[i]) {
            slot -= invSize[i];
            i++;
        }
        invList.get(i).setItem(slot, stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] slots = faceSlots[side.ordinal()];
        if (slots == null) {
            IntList intList = new IntArrayList();
            int base = 0;
            for (Container inv : invList) {
                if (inv instanceof WorldlyContainer s) {
                    int finalBase = base;
                    Arrays.stream(s.getSlotsForFace(side))
                            .map(j -> j + finalBase)
                            .forEach(intList::add);
                }
                base += inv.getContainerSize();
            }
            slots = intList.toIntArray();
            faceSlots[side.ordinal()] = slots;
        }
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        int i = 0;
        while (slot >= invSize[i]) {
            slot -= invSize[i];
            i++;
        }
        Container inv = invList.get(i);
        if (inv instanceof WorldlyContainer s) {
            return s.canPlaceItemThroughFace(slot, stack, side);
        }
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        int i = 0;
        while (slot >= invSize[i]) {
            slot -= invSize[i];
            i++;
        }
        Container inv = invList.get(i);
        if (inv instanceof WorldlyContainer s) {
            return s.canTakeItemThroughFace(slot, stack, side);
        }
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
