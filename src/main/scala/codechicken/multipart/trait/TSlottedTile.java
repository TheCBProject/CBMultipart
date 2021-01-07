package codechicken.multipart.trait;

import codechicken.multipart.api.annotation.MultiPartTrait;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.TSlottedPart;
import codechicken.multipart.block.TileMultiPart;

import java.util.Arrays;

/**
 * Created by covers1624 on 1/1/21.
 */
@MultiPartTrait (TSlottedPart.class)
public class TSlottedTile extends TileMultiPart {

    private TMultiPart[] v_partMap = new TMultiPart[27];

    @Override
    public void copyFrom(TileMultiPart that) {
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
    public TMultiPart getSlottedPart(int slot) {
        return v_partMap[slot];
    }

    @Override
    public void partRemoved(TMultiPart part, int p) {
        super.partRemoved(part, p);
        if (part instanceof TSlottedPart) {
            for (int i = 0; i < v_partMap.length; i++) {
                if (v_partMap[i] == part) {
                    v_partMap[i] = null;
                }
            }
        }
    }

    @Override
    public boolean canAddPart(TMultiPart part) {
        if (part instanceof TSlottedPart) {
            int mask = ((TSlottedPart) part).getSlotMask();
            for (int i = 0; i < v_partMap.length; i++) {
                if ((mask & 1 << i) != 0 && getSlottedPart(i) != null) {
                    return false;
                }
            }
        }

        return super.canAddPart(part);
    }

    @Override
    public void bindPart(TMultiPart part) {
        super.bindPart(part);
        if (part instanceof TSlottedPart) {
            int mask = ((TSlottedPart) part).getSlotMask();
            for (int i = 0; i < v_partMap.length; i++) {
                if ((mask & 1 << i) > 0) {
                    v_partMap[i] = part;
                }
            }
        }
    }
}
