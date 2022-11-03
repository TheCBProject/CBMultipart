package codechicken.multipart.trait;

import codechicken.multipart.api.annotation.MultiPartTrait;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.SlottedPart;
import codechicken.multipart.block.TileMultipart;

import java.util.Arrays;

/**
 * Created by covers1624 on 1/1/21.
 */
@MultiPartTrait (SlottedPart.class)
public class TSlottedTile extends TileMultipart {

    private MultiPart[] v_partMap = new MultiPart[27];

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
        if (part instanceof SlottedPart) {
            int mask = ((SlottedPart) part).getSlotMask();
            for (int i = 0; i < v_partMap.length; i++) {
                if ((mask & 1 << i) != 0 && getSlottedPart(i) != null) {
                    return false;
                }
            }
        }

        return super.canAddPart(part);
    }

    @Override
    public void bindPart(MultiPart part) {
        super.bindPart(part);
        if (part instanceof SlottedPart) {
            int mask = ((SlottedPart) part).getSlotMask();
            for (int i = 0; i < v_partMap.length; i++) {
                if ((mask & 1 << i) > 0) {
                    v_partMap[i] = part;
                }
            }
        }
    }
}
