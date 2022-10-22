package codechicken.microblock.part.corner;

import codechicken.microblock.part.MicroblockPartFactory;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.part.PlacementGrid;
import codechicken.microblock.part.PlacementProperties;

import static codechicken.microblock.part.corner.CornerPlacementGrid.CORNER_GRID;

/**
 * Created by covers1624 on 21/10/22.
 */
public class CornerPlacementProperties extends PlacementProperties {

    public static final CornerPlacementProperties CORNER_PLACEMENT = new CornerPlacementProperties();

    @Override
    public int opposite(int slot, int side) {
        return ((slot - 7) ^ (1 << (side >> 1))) + 7;
    }

    @Override
    public MicroblockPartFactory microFactory() {
        return CBMicroblockModContent.CORNER_MICROBLOCK_PART.get();
    }

    @Override
    public PlacementGrid placementGrid() {
        return CORNER_GRID;
    }
}
