package codechicken.multipart.api.part.redstone;

import codechicken.multipart.api.annotation.MultiPartMarker;
import codechicken.multipart.trait.TRedstoneTile;
import codechicken.multipart.trait.extern.IRedstoneTile;

/**
 * Interface for parts with redstone interaction
 * <p>
 * Marker interface for TRedstoneTile. This means that if a part is an instance of {@link IRedstonePart},
 * the container tile may be cast to {@link IRedstoneTile}
 */
@MultiPartMarker (TRedstoneTile.class)
public interface IRedstonePart {

    /**
     * Returns the strong (indirect, through blocks) signal being emitted by this part on the specified side.
     *
     * @param side The side index.
     * @return The redstone signal.
     */
    int strongPowerLevel(int side);

    /**
     * Returns the weak (direct) being emitted by this part on the specified side.
     *
     * @param side The side index.
     * @return The redstone signal.
     */
    int weakPowerLevel(int side);

    /**
     * Returns weather this part can connect to redstone on the specified side.
     * <p>
     * Blocking parts like covers will be handled by RedstoneInteractions.
     *
     * @param side The side index.
     * @return True if redstone can connect.
     */
    boolean canConnectRedstone(int side);

}
