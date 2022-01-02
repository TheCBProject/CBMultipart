package codechicken.multipart.api.part.render;

import codechicken.lib.render.CCRenderState;
import codechicken.multipart.api.part.IModelRenderPart;
import codechicken.multipart.api.part.TMultiPart;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * A simple {@link PartRenderer} partial implementation to render a {@link BlockState}'s {@link IBakedModel}.
 * <p>
 *
 * @see PartRenderer
 * Created by covers1624 on 7/11/21.
 */
public interface PartBakedModelRenderer<T extends TMultiPart & IModelRenderPart> extends PartRenderer<T> {

    /**
     * Returns a new {@link PartBakedModelRenderer}.
     * Use this when you don't require overriding any of the other methods provided by {@link PartRenderer}.
     *
     * @param clazz The class, Used for type checking your part class against {@link IModelRenderPart}.
     *              (This param will go away in 1.17 when the Part API is pure java.)
     * @return The {@link PartBakedModelRenderer} instance.
     */
    static <T extends TMultiPart & IModelRenderPart> PartBakedModelRenderer<T> simple(Class<? super T> clazz) {
        if (!IModelRenderPart.class.isAssignableFrom(clazz)) throw new IllegalArgumentException("Must implement IModelRenderPart.");
        return new PartBakedModelRenderer<T>() { };
    }

    @Override
    default boolean renderStatic(T part, @Nullable RenderType layer, CCRenderState ccrs) {
        if (!part.canRenderInLayer(layer)) return false;

        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        Random randy = new Random();
        return blockRenderer.renderModel(
                part.getCurrentState(),
                part.pos(),
                ccrs.lightMatrix.access,
                new MatrixStack(),
                ccrs.getConsumer(),
                true,
                randy,
                part.getModelData()
        );
    }
}
