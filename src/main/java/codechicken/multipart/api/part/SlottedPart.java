package codechicken.multipart.api.part;

/**
 * Interface for parts that fill a slot based configuration as defined in PartMap.
 * If this is implemented, calling partMap(slot) on the host tile will return this part if the corresponding bit in the slotMask is set
 * <p>
 * Marker interface for TSlottedTile
 */
public interface SlottedPart extends MultiPart {

    /**
     * a bitmask of slots that this part fills. slot x is 1<<x
     */
    int getSlotMask();
}
