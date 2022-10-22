package codechicken.microblock.part.edge;

import codechicken.microblock.part.MicroblockPartFactory;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.part.*;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.util.PartMap;
import codechicken.multipart.util.PartRayTraceResult;

/**
 * Created by covers1624 on 21/10/22.
 */
public class EdgePlacementProperties extends PlacementProperties {

    public static final EdgePlacementProperties EDGE_PLACEMENT = new EdgePlacementProperties();

    @Override
    public MicroblockPartFactory microFactory() {
        return CBMicroblockModContent.EDGE_MICROBLOCK_PART.get();
    }

    @Override
    public PlacementGrid placementGrid() {
        return EdgePlacementGrid.EDGE_GRID;
    }

    @Override
    public int opposite(int slot, int side) {
        if (slot < 0) { // Custom placement
            return slot;
        }
        int e = slot - 15;
        return 15 + PartMap.packEdgeBits(e, PartMap.unpackEdgeBits(e) ^ (1 << (side >> 1)));
    }

    @Override
    public ExecutablePlacement customPlacement(MicroblockPlacement placement) {
        if (placement.size % 2 == 1) return null;
        PostMicroblockFactory postFactory = CBMicroblockModContent.POST_MICROBLOCK_PART.get();

        PostMicroblockPart part = postFactory.create(placement.level.isClientSide, placement.material);
        part.setShape(placement.size, placement.side >> 1);
        if (placement.doExpand) {
            TMultiPart hPart = ((PartRayTraceResult) placement.hit).part;
            if (hPart.getType() == postFactory) {
                MicroblockPart mPart = (MicroblockPart) hPart;
                if (mPart.material == placement.material && mPart.getSize() + placement.size < 8) {
                    part.shape = (byte) ((mPart.getSize() + placement.size) << 4 | mPart.getShapeSlot());
                    return placement.expand(mPart, part);
                }
            }
        }

        if (placement.slot >= 0) return null;

        if (placement.internal && !placement.oppMod) {
            return placement.internalPlacement(placement.hTile, part);
        }

        return placement.externalPlacement(part);
    }
}
