package codechicken.multipart.trait;

import codechicken.lib.math.MathHelper;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.NeighborTileChangePart;
import codechicken.multipart.block.TileMultipart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Created by covers1624 on 23/9/20.
 */
public class TTileChangeTile extends TileMultipart {

    private boolean weakTileChanges = false;

    public TTileChangeTile(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void copyFrom(TileMultipart that) {
        super.copyFrom(that);
        if (that instanceof TTileChangeTile) {
            weakTileChanges = ((TTileChangeTile) that).weakTileChanges;
        }
    }

    @Override
    public void bindPart(MultiPart part) {
        super.bindPart(part);
        if (part instanceof NeighborTileChangePart) {
            weakTileChanges |= ((NeighborTileChangePart) part).weakTileChanges();
        }
    }

    @Override
    public void partRemoved(MultiPart part, int p) {
        super.partRemoved(part, p);
        weakTileChanges = getPartList().stream()
                .anyMatch(e -> e instanceof NeighborTileChangePart && ((NeighborTileChangePart) e).weakTileChanges());
    }

    @Override
    public boolean getWeakChanges() {
        return super.getWeakChanges() || weakTileChanges;
    }

    @Override
    public void onNeighborTileChange(BlockPos neighborPos) {
        super.onNeighborTileChange(neighborPos);

        BlockPos offset = neighborPos.subtract(getBlockPos());
        int diff = MathHelper.absSum(offset);
        Direction side = MathHelper.getSide(offset);

        if (side == null || diff <= 0 || diff > 2) {
            return;
        }
        boolean weak = diff == 2;
        List<MultiPart> jPartList = getPartList();
        for (MultiPart part : jPartList) {
            if (part instanceof NeighborTileChangePart) {
                ((NeighborTileChangePart) part).onNeighborTileChanged(side, weak);
            }
        }
    }
}
