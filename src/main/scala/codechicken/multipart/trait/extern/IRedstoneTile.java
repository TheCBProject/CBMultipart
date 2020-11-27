package codechicken.multipart.trait.extern;

import codechicken.multipart.api.tile.IRedstoneConnector;

/**
 * Internal interface for TileMultipart instances hosting IRedstonePart
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
