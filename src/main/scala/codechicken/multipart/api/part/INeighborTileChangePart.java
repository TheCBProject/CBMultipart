package codechicken.multipart.api.part;

import net.minecraft.util.Direction;

/**
 * Mixin interface for parts that want to be notified of neighbor tile change events (comparators or inventory maintainers)
 */
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
