package codechicken.multipart.trait;

import codechicken.multipart.api.annotation.MultiPartTrait;
import codechicken.multipart.api.part.ITickablePart;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.trait.extern.ITickableTile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by covers1624 on 18/9/20.
 */
@MultiPartTrait (ITickablePart.class)
public class TTickableTile extends TileMultiPart implements ITickableTile {

    private final List<ITickablePart> tickingParts = new ArrayList<>();

    @Override
    public void copyFrom(TileMultiPart that) {
        super.copyFrom(that);
        if (that instanceof TTickableTile) {
            tickingParts.clear();
            tickingParts.addAll(((TTickableTile) that).tickingParts);
        }
    }

    @Override
    public void bindPart(TMultiPart part) {
        super.bindPart(part);
        if (part instanceof ITickablePart p) {
            tickingParts.add(p);
        }
    }

    @Override
    public void partRemoved(TMultiPart part, int p) {
        super.partRemoved(part, p);
        if (part instanceof ITickablePart) {
            tickingParts.remove(part);
        }
    }

    @Override
    public void clearParts() {
        super.clearParts();
        tickingParts.clear();
    }

    @Override
    public void tick() {
        getCapCache().tick();
        for (int i = 0, tickingPartsSize = tickingParts.size(); i < tickingPartsSize; i++) {
            ITickablePart part = tickingParts.get(i);
            if (((TMultiPart) part).tile() != null) {
                part.tick();
            }
        }
    }
}
