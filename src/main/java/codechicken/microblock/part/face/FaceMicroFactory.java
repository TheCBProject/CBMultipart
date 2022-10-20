package codechicken.microblock.part.face;

import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.factory.StandardMicroFactory;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.part.PlacementProperties;
import codechicken.multipart.api.MultiPartType;

import static codechicken.microblock.part.face.FacePlacementProperties.FACE_PLACEMENT;

/**
 * Created by covers1624 on 17/10/22.
 */
public class FaceMicroFactory extends StandardMicroFactory {

    public FaceMicroFactory() {
        super(0);
    }

    @Override
    public MultiPartType<?> getType() {
        return CBMicroblockModContent.FACE_MICROBLOCK_PART.get();
    }

    @Override
    public FaceMicroblockPart create(boolean client, MicroMaterial material) {
        return new FaceMicroblockPart(material);
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
