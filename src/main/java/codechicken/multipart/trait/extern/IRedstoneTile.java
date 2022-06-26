package codechicken.multipart.trait.extern;

import codechicken.multipart.api.part.redstone.IRedstonePart;
import codechicken.multipart.api.tile.IRedstoneConnector;
import codechicken.multipart.block.TileMultiPart;

/**
 * Internal interface for {@link TileMultiPart} instances hosting {@link IRedstonePart}s
 */
public interface IRedstoneTile extends IRedstoneConnector {

    /**
     * Returns the mask of spaces through which a wire could connect on a side.
     *
     * @param side The side index.
     * @return The mask.
     */
    int openConnections(int side);
}
