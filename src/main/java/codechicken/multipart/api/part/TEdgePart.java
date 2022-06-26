package codechicken.multipart.api.part;

/**
 * Interface which must be implemented by parts that go in an edge slot.
 */
public interface TEdgePart extends TSlottedPart {

    /**
     * Return true if this part can conduct redstone signal or let redstone signal pass through it.
     */
    default boolean conductsRedstone() {
        return false;
    }
}
