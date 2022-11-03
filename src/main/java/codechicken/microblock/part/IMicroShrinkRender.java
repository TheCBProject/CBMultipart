package codechicken.microblock.part;

import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.api.part.MultiPart;

/**
 * Created by covers1624 on 10/7/22.
 */
public interface IMicroShrinkRender extends MultiPart {

    int getPriorityClass();

    int getSlot();

    int getSize();

    boolean isTransparent();

    Cuboid6 getBounds();
}
