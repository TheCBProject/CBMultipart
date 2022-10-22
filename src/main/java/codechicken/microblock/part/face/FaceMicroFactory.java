package codechicken.microblock.part.face;

import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.part.StandardMicroFactory;
import codechicken.microblock.part.PlacementProperties;

import static codechicken.microblock.part.face.FacePlacementProperties.FACE_PLACEMENT;

/**
 * Created by covers1624 on 17/10/22.
 */
public class FaceMicroFactory extends StandardMicroFactory {

    public FaceMicroFactory() {
        super(0);
    }

    @Override
    public FaceMicroblockPart create(boolean client, MicroMaterial material) {
        return new FaceMicroblockPart(material);
    }

    @Override
    public float getResistanceFactor() {
        return 1;
    }

    @Override
    public PlacementProperties placementProperties() {
        return FACE_PLACEMENT;
    }

    @Override
    public int getItemSlot() {
        return 3;
    }
}
