package codechicken.microblock.part.edge;

import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.factory.MicroblockPartFactory;

/**
 * Created by covers1624 on 21/10/22.
 */
public class PostMicroblockFactory extends MicroblockPartFactory {

    @Override
    public PostMicroblockPart create(boolean client, MicroMaterial material) {
        return new PostMicroblockPart(material);
    }
}
