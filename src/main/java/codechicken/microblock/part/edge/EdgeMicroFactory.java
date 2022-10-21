package codechicken.microblock.part.edge;

import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.factory.StandardMicroFactory;
import codechicken.microblock.part.PlacementProperties;
import codechicken.microblock.part.StandardMicroblockPart;

import static codechicken.microblock.part.edge.EdgePlacementProperties.EDGE_PLACEMENT;

/**
 * Created by covers1624 on 21/10/22.
 */
public class EdgeMicroFactory extends StandardMicroFactory {

    public EdgeMicroFactory() {
        super(3);
    }

    @Override
    public PlacementProperties placementProperties() {
        return EDGE_PLACEMENT;
    }

    @Override
    public StandardMicroblockPart create(boolean client, MicroMaterial material) {
        return new EdgeMicroblockPart(material);
    }

    @Override
    public float getResistanceFactor() {
        return 0.5F;
    }

    @Override
    public int getItemSlot() {
        return 15;
    }
}
