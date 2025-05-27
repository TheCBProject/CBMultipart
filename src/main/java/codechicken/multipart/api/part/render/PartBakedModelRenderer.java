package codechicken.multipart.api.part.render;

import codechicken.lib.render.CCRenderState;
import codechicken.multipart.api.part.ModelRenderPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A simple {@link PartRenderer} partial implementation to render a {@link BlockState}'s {@link BakedModel}.
 * <p>
 *
 * @see PartRenderer
 * Created by covers1624 on 7/11/21.
 */
// TODO 1.21.4, Refactor to BlockStatePartBakedModelRenderer
public interface PartBakedModelRenderer<T extends ModelRenderPart> extends PartRenderer<T> {

    /**
     * Returns a new {@link PartBakedModelRenderer}.
     * Use this when you don't require overriding any of the other methods provided by {@link PartRenderer}.
     *
     * @return The {@link PartBakedModelRenderer} instance.
     */
    static <T extends ModelRenderPart> PartBakedModelRenderer<T> simple() {
        return new PartBakedModelRenderer<>() { };
    }

    @Override
    default List<BakedQuad> getQuads(T part, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BlockState state = part.getCurrentState();
        RandomSource randy = RandomSource.create();
        BakedModel model = blockRenderer.getBlockModel(state);

        if (renderType != null && !model.getRenderTypes(state, randy, data).contains(renderType)) return List.of();

        return model.getQuads(state, side, rand, data, renderType);
    }

    @Override
    @SuppressWarnings ("ConstantConditions")
    default void renderStatic(T part, @Nullable RenderType layer, CCRenderState ccrs) {

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BlockState state = part.getCurrentState();
        RandomSource randy = RandomSource.create();

        if (layer != null && !blockRenderer.getBlockModel(state).getRenderTypes(state, randy, part.getModelData()).contains(layer)) return;

        blockRenderer.renderBatched(
                part.getCurrentState(),
                part.pos(),
                ccrs.lightMatrix.access,
                new PoseStack(),
                ccrs.getConsumer(),
                true,
                randy,
                part.getModelData(),
                layer
        );
    }
}
