package codechicken.microblock.part;

import codechicken.lib.vec.Cuboid6;
import codechicken.microblock.api.MicroMaterial;
import codechicken.multipart.api.part.SlottedPart;

/**
 * Created by covers1624 on 10/7/22.
 */
public interface IMicroOcclusion extends SlottedPart {

    int getSlot();

    int getSize();

    MicroMaterial getMaterial();

    Cuboid6 getBounds();
}
