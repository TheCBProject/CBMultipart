package codechicken.microblock.part.hollow;

import codechicken.microblock.factory.MicroblockPartFactory;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.part.PlacementGrid;
import codechicken.microblock.part.face.FaceEdgeGrid;
import codechicken.microblock.part.face.FacePlacementProperties;

/**
 * Created by covers1624 on 20/10/22.
 */
public class HollowPlacementProperties extends FacePlacementProperties {

    public static final HollowPlacementProperties HOLLOW_PLACEMENT = new HollowPlacementProperties();

    @Override
    public MicroblockPartFactory microFactory() {
        return CBMicroblockModContent.HOLLOW_MICROBLOCK_PART.get();
    }

    @Override
    public PlacementGrid placementGrid() {
        return FaceEdgeGrid.HOLLOW_PLACEMENT_GRID;
    }
}
