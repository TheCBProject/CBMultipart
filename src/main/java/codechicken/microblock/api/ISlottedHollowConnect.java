package codechicken.microblock.api;

import org.jetbrains.annotations.Range;

/**
 * Implement on center attached parts that can connect through Hollow covers to adjust the hole size of the cover.
 */
public interface ISlottedHollowConnect {

    /**
     * @param side The side of the block on which the cover resides.
     * @return The size (width and height) of the connection in pixels. Must be less than 12 and more than 0.
     */
    @Range(from = 1, to = 11)
    int getHoleSize(int side);
}
