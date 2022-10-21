package codechicken.microblock.part.hollow;

import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.factory.StandardMicroFactory;
import codechicken.microblock.part.PlacementProperties;
import codechicken.microblock.part.StandardMicroblockPart;

import static codechicken.microblock.part.hollow.HollowPlacementProperties.HOLLOW_PLACEMENT;

/**
 * Created by covers1624 on 20/10/22.
 */
public class HollowMicroFactory extends StandardMicroFactory {

    public HollowMicroFactory() {
        super(1);
    }

    @Override
    public PlacementProperties placementProperties() {
        return HOLLOW_PLACEMENT;
    }

    @Override
    public StandardMicroblockPart create(boolean client, MicroMaterial material) {
        return new HollowMicroblockPart(material);
    }

    @Override
    public int getItemSlot() {
        return 3;
    }
}
