package codechicken.multipart.api.part;

/**
 * Interface which must be implemented by parts that go in a face part.
 */
public interface TFacePart extends TSlottedPart {

    /**
     * Passed down from Block.isSideSolid. Return true if this part is solid and opaque on the specified side
     */
    default boolean solid(int side) {
        return true;
    }

    /**
     * Return the redstone conduction map for which signal can pass through this part on the face.
     * Eg, hollow covers return 0x10 as signal can pass through the center hole.
     */
    default int redstoneConductionMap() {
        return 0;
    }
}
