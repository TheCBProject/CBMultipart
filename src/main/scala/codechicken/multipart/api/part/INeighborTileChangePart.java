package codechicken.multipart.api.part;

import codechicken.multipart.api.annotation.MultiPartMarker;
import codechicken.multipart.trait.TTileChangeTile;
import net.minecraft.util.Direction;

/**
 * Mixin interface for parts that want to be notified of neighbor tile change events (comparators or inventory maintainers)
 */
@MultiPartMarker (TTileChangeTile.class)
public interface INeighborTileChangePart {

    /**
     * Returns whether this part needs calls for tile changes through one solid block
     */
    boolean weakTileChanges();

    /**
     * Callback for neighbor tile changes, from same function in Block
     */
    void onNeighborTileChanged(Direction side, boolean weak);
}
