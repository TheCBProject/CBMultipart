package codechicken.multipart.api.part.render;

import codechicken.multipart.api.RegisterMultipartRenderersEvent;
import codechicken.multipart.api.part.MultiPart;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.List;

/**
 * Created by covers1624 on 7/26/26.
 *
 * @see RegisterMultipartRenderersEvent
 */
public interface StaticPartRenderer<P extends MultiPart> {

    /**
     * Get the static quads for this part, this is synonymous to {@link BlockStateModel#collectParts(BlockAndTintGetter, BlockPos, BlockState, RandomSource, List)}
     * <p>
     * This is method may be called on the chunk batching thread. World/state access should be performed in a thread-safe manner.
     * <p>
     * It is highly recommended that parts do some form of caching for the data returned here.
     *
     * @param modelData The model data you returned from {@link MultiPart#getModelData()}.
     * @param level     The level.
     * @param pos       The block position.
     * @param rand      The {@link RandomSource} for this block position.
     * @param parts     The output collector.
     */
    void collectParts(ModelData modelData, BlockAndTintGetter level, BlockPos pos, RandomSource rand, List<BlockModelPart> parts);

    @FunctionalInterface
    interface Factory<P extends MultiPart> {

        StaticPartRenderer<P> create();
    }
}
