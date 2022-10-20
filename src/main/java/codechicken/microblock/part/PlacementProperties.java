package codechicken.microblock.part;

import codechicken.microblock.factory.MicroblockPartFactory;

/**
 * Created by covers1624 on 20/10/22.
 */
public abstract class PlacementProperties {

    public abstract int opposite(int slot, int side);

    public boolean sneakOpposite(int slot, int side) {
        return true;
    }

    public boolean expand(int slot, int size) {
        return true;
    }

    public abstract MicroblockPartFactory microFactory();

    public abstract PlacementGrid placementGrid();

    public ExecutablePlacement customPlacement(MicroblockPlacement placement) {
        return null;
    }
}
