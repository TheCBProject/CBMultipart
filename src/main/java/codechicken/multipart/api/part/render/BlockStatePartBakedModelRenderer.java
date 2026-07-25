package codechicken.multipart.api.part.render;

import codechicken.multipart.api.part.BlockStateModelPart;
import codechicken.multipart.util.ProxyBlockAndTintGetter;
import codechicken.multipart.util.WrappedBlockModelPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.List;

/**
 * A simple {@link PartRenderer} partial implementation to render a {@link BlockState}'s {@link BlockStateModel}.
 * <p>
 *
 * @see PartRenderer
 * Created by covers1624 on 7/11/21.
 */
public interface BlockStatePartBakedModelRenderer<T extends BlockStateModelPart> extends PartRenderer<T, Void> {

    /**
     * Returns a new {@link BlockStatePartBakedModelRenderer}.
     * Use this when you don't require overriding any of the other methods provided by {@link PartRenderer}.
     *
     * @return The {@link BlockStatePartBakedModelRenderer} instance.
     */
    static <T extends BlockStateModelPart> BlockStatePartBakedModelRenderer<T> simple() {
        return new BlockStatePartBakedModelRenderer<>() { };
    }

    @Override
    default void collectParts(ModelData modelData, BlockAndTintGetter level, BlockPos pos, RandomSource rand, List<BlockModelPart> parts) {
        var data = modelData.get(BlockStateModelPart.BlockStateModelPartData.TYPE);
        if (data == null) return;

        var state = data.state();
        var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        // TODO we can likely cache the list used for this collectParts call somewhere if we use an instance class?
        var modelParts = model.collectParts(
                new ProxyBlockAndTintGetter(level, pos, state, modelData),
                pos,
                state,
                rand
        );
        for (var modelPart : modelParts) {
            parts.add(new WrappedBlockModelPart(modelPart, state));
        }

    }
}
