package codechicken.microblock.util;

import codechicken.lib.vec.Cuboid6;

/**
 * Created by covers1624 on 20/10/22.
 */
public record MaskedCuboid(Cuboid6 box, int sideMask) {
}
