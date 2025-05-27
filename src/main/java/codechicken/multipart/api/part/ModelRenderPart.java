package codechicken.multipart.api.part;

import codechicken.multipart.api.part.render.PartBakedModelRenderer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Companion to {@link PartBakedModelRenderer}
 */
// TODO 1.21.4, Refactor this to `BlockStateModelPart`
public interface ModelRenderPart extends MultiPart {

    BlockState getCurrentState();

    @Override
    default ModelData getModelData() {
        return ModelData.EMPTY;
    }
}
