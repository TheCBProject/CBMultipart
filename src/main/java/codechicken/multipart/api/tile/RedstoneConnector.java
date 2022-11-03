package codechicken.multipart.api.tile;

/**
 * Interface for tile entities which split their redstone connections into a mask for each side (edges and center)
 *
 * All connection masks are a 5 bit map.
 * The lowest 4 bits correspond to the connection toward the face specified Rotation.rotateSide(side & 6, b) where b is the bit index from lowest to highest.
 * Bit 5 corresponds to a connection opposite side.
 */
public interface RedstoneConnector {

    /**
     * Returns the connection mask of this tile for the given side.
     *
     * @param side The side index.
     * @return The connection mask.
     */
    int getConnectionMask(int side);

    /**
     * Returns the weak power level provided by this tile on the given side, through the given mask.
     *
     * @param side The side index.
     * @param mask The connection mask.
     * @return The redstone signal.
     */
    int weakPowerLevel(int side, int mask);
}
