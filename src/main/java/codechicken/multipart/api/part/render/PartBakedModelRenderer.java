package codechicken.multipart.api.part.render;

import codechicken.lib.render.CCRenderState;
import codechicken.multipart.api.part.IModelRenderPart;
import codechicken.multipart.api.part.TMultiPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * A simple {@link PartRenderer} partial implementation to render a {@link BlockState}'s {@link BakedModel}.
 * <p>
 *
 * @see PartRenderer
 * Created by covers1624 on 7/11/21.
 */
public interface PartBakedModelRenderer<T extends IModelRenderPart> extends PartRenderer<T> {

    /**
     * Returns a new {@link PartBakedModelRenderer}.
     * Use this when you don't require overriding any of the other methods provided by {@link PartRenderer}.
     *
     * @return The {@link PartBakedModelRenderer} instance.
     */
    static <T extends IModelRenderPart> PartBakedModelRenderer<T> simple() {
        return new PartBakedModelRenderer<>() { };
    }

    @Override
    default boolean renderStatic(T part, @Nullable RenderType layer, CCRenderState ccrs) {
        if (!part.canRenderInLayer(layer)) return false;

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        Random randy = new Random();
        return blockRenderer.renderBatched(
                part.getCurrentState(),
                part.pos(),
                ccrs.lightMatrix.access,
                new PoseStack(),
                ccrs.getConsumer(),
                true,
                randy,
                part.getModelData()
        );
    }
}
