package codechicken.microblock.part.corner;

import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.factory.StandardMicroFactory;
import codechicken.microblock.part.PlacementProperties;
import codechicken.microblock.part.StandardMicroblockPart;

/**
 * Created by covers1624 on 21/10/22.
 */
public class CornerMicroFactory extends StandardMicroFactory {

    public CornerMicroFactory() {
        super(2);
    }

    @Override
    public PlacementProperties placementProperties() {
        return CornerPlacementProperties.CORNER_PLACEMENT;
    }

    @Override
    public StandardMicroblockPart create(boolean client, MicroMaterial material) {
        return new CornerMicroblockPart(material);
    }

    @Override
    public float getResistanceFactor() {
        return 1;
    }

    @Override
    public int getItemSlot() {
        return 7;
    }
}
