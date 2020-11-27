package codechicken.multipart.api.part.redstone;

import codechicken.multipart.api.part.TFacePart;

/**
 * For parts like wires that adhere to a specific face, reduces redstone connections to the specific edge between two faces.
 * Should be implemented on parts implementing {@link TFacePart}
 */
public interface IFaceRedstonePart extends IRedstonePart {

    /**
     * Returns the face which this Redstone part is attached.
     *
     * @return The face.
     */
    int getFace();
}
