package codechicken.multipart.trait;

import codechicken.multipart.api.part.TickablePart;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.api.TickableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by covers1624 on 18/9/20.
 */
public class TTickableTile extends TileMultipart implements TickableTile {

    private final List<TickablePart> tickingParts = new ArrayList<>();

    public TTickableTile(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void copyFrom(TileMultipart that) {
        super.copyFrom(that);
        if (that instanceof TTickableTile) {
            tickingParts.clear();
            tickingParts.addAll(((TTickableTile) that).tickingParts);
        }
    }

    @Override
    public void bindPart(MultiPart part) {
        super.bindPart(part);
        if (part instanceof TickablePart p) {
            tickingParts.add(p);
        }
    }

    @Override
    public void partRemoved(MultiPart part, int p) {
        super.partRemoved(part, p);
        if (part instanceof TickablePart) {
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
            TickablePart part = tickingParts.get(i);
            if (((MultiPart) part).tile() != null) {
                part.tick();
            }
        }
    }
}
