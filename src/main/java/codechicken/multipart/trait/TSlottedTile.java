package codechicken.multipart.trait;

import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.SlottedPart;
import codechicken.multipart.block.TileMultipart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

/**
 * Created by covers1624 on 1/1/21.
 */
public class TSlottedTile extends TileMultipart {

    private MultiPart[] v_partMap = new MultiPart[27];

    public TSlottedTile(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void copyFrom(TileMultipart that) {
        super.copyFrom(that);
        if (that instanceof TSlottedTile) {
            v_partMap = ((TSlottedTile) that).v_partMap;
        }
    }

    @Override
    public void clearParts() {
        super.clearParts();
        Arrays.fill(v_partMap, null);
    }

    @Override
    public MultiPart getSlottedPart(int slot) {
        return v_partMap[slot];
    }

    @Override
    public void partRemoved(MultiPart part, int p) {
        super.partRemoved(part, p);
        if (part instanceof SlottedPart) {
            for (int i = 0; i < v_partMap.length; i++) {
                if (v_partMap[i] == part) {
                    v_partMap[i] = null;
                }
            }
        }
    }

    @Override
    public boolean canAddPart(MultiPart part) {
        if (part instanceof SlottedPart slottedPart) {
            int mask = slottedPart.getSlotMask();
            for (int i = 0; i < v_partMap.length; i++) {
                if ((mask & 1 << i) != 0 && getSlottedPart(i) != null) {
                    return false;
                }
            }
        }

        return super.canAddPart(part);
    }

    private void addToPartMap(SlottedPart part) {
        int mask = part.getSlotMask();
        for (int i = 0; i < v_partMap.length; i++) {
            if ((mask & 1 << i) > 0) {
                v_partMap[i] = part;
            }
        }
    }

    @Override
    public void bindPart(MultiPart part) {
        super.bindPart(part);
        if (part instanceof SlottedPart slottedPart) {
            addToPartMap(slottedPart);
        }
    }

    @Override
    public void onTransform(Direction.Axis rotationAxis, Rotation rotation, Mirror mirror) {
        super.onTransform(rotationAxis, rotation, mirror);

        Arrays.fill(v_partMap, null);
        operate(part -> {
            if (part instanceof SlottedPart slottedPart) {
                addToPartMap(slottedPart);
            }
        });
    }
}
