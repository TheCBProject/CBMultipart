package codechicken.microblock.part.face;

import codechicken.microblock.part.MicroblockPartFactory;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.part.PlacementGrid;
import codechicken.microblock.part.PlacementProperties;

/**
 * Created by covers1624 on 20/10/22.
 */
public class FacePlacementProperties extends PlacementProperties {

    public static final FacePlacementProperties FACE_PLACEMENT = new FacePlacementProperties();

    @Override
    public int opposite(int slot, int side) {
        return slot ^ 1;
    }

    @Override
    public boolean expand(int slot, int size) {
        return sneakOpposite(slot, size);
    }

    @Override
    public boolean sneakOpposite(int slot, int side) {
        return slot == (side ^ 1);
    }

    @Override
    public MicroblockPartFactory microFactory() {
        return CBMicroblockModContent.FACE_MICROBLOCK_PART.get();
    }

    @Override
    public PlacementGrid placementGrid() {
        return FaceEdgeGrid.FACE_PLACEMENT_GRID;
    }
}
