package codechicken.microblock.part;

import codechicken.lib.vec.Cuboid6;
import codechicken.microblock.api.MicroMaterial;
import codechicken.multipart.api.part.TSlottedPart;

/**
 * Created by covers1624 on 10/7/22.
 */
public interface IMicroOcclusion extends TSlottedPart {

    int getSlot();

    int getSize();

    MicroMaterial getMaterial();

    Cuboid6 getBounds();
}
