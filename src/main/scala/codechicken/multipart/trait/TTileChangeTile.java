package codechicken.multipart.trait;

import codechicken.lib.math.MathHelper;
import codechicken.mixin.forge.TraitSide;
import codechicken.multipart.api.annotation.MultiPartTrait;
import codechicken.multipart.api.part.INeighborTileChangePart;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.TileMultiPart;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Created by covers1624 on 23/9/20.
 */
@MultiPartTrait (value = INeighborTileChangePart.class, side = TraitSide.SERVER)
public class TTileChangeTile extends TileMultiPart {

    private boolean weakTileChanges = false;

    @Override
    public void copyFrom(TileMultiPart that) {
        super.copyFrom(that);
        if (that instanceof TTileChangeTile) {
            weakTileChanges = ((TTileChangeTile) that).weakTileChanges;
        }
    }

    @Override
    public void bindPart(TMultiPart part) {
        super.bindPart(part);
        if (part instanceof INeighborTileChangePart) {
            weakTileChanges |= ((INeighborTileChangePart) part).weakTileChanges();
        }
    }

    @Override
    public void partRemoved(TMultiPart part, int p) {
        super.partRemoved(part, p);
        weakTileChanges = getPartList().stream()
                .anyMatch(e -> e instanceof INeighborTileChangePart && ((INeighborTileChangePart) e).weakTileChanges());
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
        List<TMultiPart> jPartList = getPartList();
        for (int i = 0, jPartListSize = jPartList.size(); i < jPartListSize; i++) {
            TMultiPart part = jPartList.get(i);
            if (part instanceof INeighborTileChangePart) {
                ((INeighborTileChangePart) part).onNeighborTileChanged(side, weak);
            }
        }
    }
}
