package codechicken.multipart.api.part;

import codechicken.multipart.api.part.render.BlockStatePartBakedModelRenderer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

/**
 * A part which is ultimately backed by a {@link BlockState}.
 *
 * @see BlockStatePartBakedModelRenderer
 */
public interface BlockStateModelPart extends MultiPart {

    BlockState getCurrentState();

    @Override
    default ModelData getModelData() {
        return MultiPart.super.getModelData().derive()
                .with(BlockStateModelPartData.TYPE, new BlockStateModelPartData(getCurrentState()))
                .build();
    }

    record BlockStateModelPartData(BlockState state) {

        public static final ModelProperty<BlockStateModelPartData> TYPE = new ModelProperty<>();
    }
}
