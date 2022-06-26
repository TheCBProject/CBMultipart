package codechicken.multipart.api.part.redstone;

/**
 * For parts that want to define their own connection masks (like center-center parts)
 */
public interface IMaskedRedstonePart extends IRedstonePart {

    /**
     * Returns the redstone connection mask for this part on the given side.
     * <p>
     * see IRedstoneConnector for mask definition.
     *
     * @param side The side to get the mask for.
     * @return The mask.
     */
    int getConnectionMask(int side);
}
