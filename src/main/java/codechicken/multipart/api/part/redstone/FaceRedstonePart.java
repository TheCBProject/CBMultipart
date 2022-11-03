package codechicken.multipart.api.part.redstone;

import codechicken.multipart.api.part.FacePart;

/**
 * For parts like wires that adhere to a specific face, reduces redstone connections to the specific edge between two faces.
 * Should be implemented on parts implementing {@link FacePart}
 */
public interface FaceRedstonePart extends RedstonePart {

    /**
     * Returns the face which this Redstone part is attached.
     *
     * @return The face.
     */
    int getFace();
}
