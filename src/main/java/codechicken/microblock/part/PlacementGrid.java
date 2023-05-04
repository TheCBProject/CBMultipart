package codechicken.microblock.part;

import codechicken.lib.vec.Line3;
import codechicken.lib.vec.Vector3;

import java.util.List;

/**
 * Created by covers1624 on 20/10/22.
 */
public abstract class PlacementGrid {

    public abstract int getHitSlot(Vector3 vHit, int side);

    public abstract List<Line3> getOverlayLines();
}
