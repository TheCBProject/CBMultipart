package codechicken.multipart.api.part;

import codechicken.multipart.api.part.render.PartBakedModelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

/**
 * Companion to {@link PartBakedModelRenderer}
 */
public interface IModelRenderPart extends TMultiPart {

    boolean canRenderInLayer(RenderType layer);

    BlockState getCurrentState();

    default IModelData getModelData() {
        return EmptyModelData.INSTANCE;
    }
}
