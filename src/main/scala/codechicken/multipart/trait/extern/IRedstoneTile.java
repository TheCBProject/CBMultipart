package codechicken.multipart.trait.extern;

import codechicken.multipart.TileMultipart;
import codechicken.multipart.api.part.redstone.IRedstonePart;
import codechicken.multipart.api.tile.IRedstoneConnector;

/**
 * Internal interface for {@link TileMultipart} instances hosting {@link IRedstonePart}s
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
