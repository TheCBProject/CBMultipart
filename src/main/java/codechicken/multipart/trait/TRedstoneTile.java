package codechicken.multipart.trait;

import codechicken.multipart.api.annotation.MultiPartTrait;
import codechicken.multipart.api.part.EdgePart;
import codechicken.multipart.api.part.FacePart;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.redstone.RedstonePart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.trait.extern.RedstoneTile;

import static codechicken.lib.vec.Rotation.rotateSide;
import static codechicken.multipart.api.RedstoneInteractions.connectionMask;
import static codechicken.multipart.api.RedstoneInteractions.otherConnectionMask;
import static codechicken.multipart.util.PartMap.edgeBetween;

/**
 * Created by covers1624 on 31/12/20.
 */
@MultiPartTrait (RedstonePart.class)
public class TRedstoneTile extends TileMultipart implements RedstoneTile {

    @Override
    public int getDirectSignal(int side) {
        int max = 0;
        for (MultiPart part : getPartList()) {
            if (part instanceof RedstonePart) {
                int l = ((RedstonePart) part).strongPowerLevel(side);
                if (l > max) {
                    max = l;
                }
            }
        }
        return max;
    }

    @Override
    public int getSignal(int side) {
        return weakPowerLevel(side, otherConnectionMask(getLevel(), getBlockPos(), side, true));
    }

    @Override
    public boolean canConnectRedstone(int side) {
        return (getConnectionMask(side) & otherConnectionMask(getLevel(), getBlockPos(), side, true)) > 0;
    }

    @Override
    public int getConnectionMask(int side) {
        int mask = openConnections(side);
        int res = 0;
        for (MultiPart part : getPartList()) {
            res |= connectionMask(part, side) & mask;
        }
        return res;
    }

    @Override
    public int weakPowerLevel(int side, int mask) {
        int tMask = openConnections(side) & mask;
        int max = 0;
        for (MultiPart part : getPartList()) {
            if ((connectionMask(part, side) & tMask) > 0) {
                int l = ((RedstonePart) part).weakPowerLevel(side);
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
        MultiPart part = getSlottedPart(i);
        if (part instanceof FacePart) {
            return ((FacePart) part).redstoneConductionMap();
        }
        return 0x1F;
    }

    private boolean redstoneConductionE(int i) {
        MultiPart part = getSlottedPart(i);
        if (part instanceof EdgePart) {
            return ((EdgePart) part).conductsRedstone();
        }
        return true;
    }
}
