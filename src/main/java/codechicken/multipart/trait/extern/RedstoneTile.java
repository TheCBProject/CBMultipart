package codechicken.multipart.trait.extern;

import codechicken.multipart.api.part.redstone.RedstonePart;
import codechicken.multipart.api.tile.RedstoneConnector;
import codechicken.multipart.block.TileMultipart;

/**
 * Internal interface for {@link TileMultipart} instances hosting {@link RedstonePart}s
 */
public interface RedstoneTile extends RedstoneConnector {

    /**
     * Returns the mask of spaces through which a wire could connect on a side.
     *
     * @param side The side index.
     * @return The mask.
     */
    int openConnections(int side);
}
