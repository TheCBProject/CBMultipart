package codechicken.multipart.trait;

import codechicken.multipart.api.annotation.MultiPartTrait;
import codechicken.multipart.api.part.TEdgePart;
import codechicken.multipart.api.part.TFacePart;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.redstone.IRedstonePart;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.trait.extern.IRedstoneTile;

import static codechicken.lib.vec.Rotation.rotateSide;
import static codechicken.multipart.api.RedstoneInteractions.connectionMask;
import static codechicken.multipart.api.RedstoneInteractions.otherConnectionMask;
import static codechicken.multipart.util.PartMap.edgeBetween;

/**
 * Created by covers1624 on 31/12/20.
 */
@MultiPartTrait (IRedstonePart.class)
public class TRedstoneTile extends TileMultiPart implements IRedstoneTile {

    @Override
    public int strongPowerLevel(int side) {
        int max = 0;
        for (TMultiPart part : getPartList()) {
            if (part instanceof IRedstonePart) {
                int l = ((IRedstonePart) part).strongPowerLevel(side);
                if (l > max) {
                    max = l;
                }
            }
        }
        return max;
    }

    @Override
    public int weakPowerLevel(int side) {
        return weakPowerLevel(side, otherConnectionMask(getWorld(), getPos(), side, true));
    }

    @Override
    public boolean canConnectRedstone(int side) {
        return (getConnectionMask(side) & otherConnectionMask(getWorld(), getPos(), side, true)) > 0;
    }

    @Override
    public int getConnectionMask(int side) {
        int mask = openConnections(side);
        int res = 0;
        for (TMultiPart part : getPartList()) {
            res |= connectionMask(part, side) & mask;
        }
        return res;
    }

    @Override
    public int weakPowerLevel(int side, int mask) {
        int tMask = openConnections(side) & mask;
        int max = 0;
        for (TMultiPart part : getPartList()) {
            if ((connectionMask(part, side) & tMask) > 0) {
                int l = ((IRedstonePart) part).weakPowerLevel(side);
                if (l > max) {
                    max = l;
                }
            }
        }
        return max;
    }

    @Override
    public int openConnections(int side) {
        int m = 0x10;

        for (int i = 0; i < 4; i++) {
            if (redstoneConductionE(edgeBetween(side, rotateSide(side & 6, i)))) {
                m |= 1 << i;
            }
        }

        m &= redstoneConductionF(side);
        return m;
    }

    private int redstoneConductionF(int i) {
        TMultiPart part = getSlottedPart(i);
        if (part instanceof TFacePart) {
            return ((TFacePart) part).redstoneConductionMap();
        }
        return 0x1F;
    }

    private boolean redstoneConductionE(int i) {
        TMultiPart part = getSlottedPart(i);
        if (part instanceof TEdgePart) {
            return ((TEdgePart) part).conductsRedstone();
        }
        return true;
    }
}
