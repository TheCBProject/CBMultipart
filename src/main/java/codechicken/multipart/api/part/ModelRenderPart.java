package codechicken.multipart.api.part;

import codechicken.multipart.api.part.render.PartBakedModelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.Nullable;

/**
 * Companion to {@link PartBakedModelRenderer}
 */
public interface ModelRenderPart extends MultiPart {

    boolean canRenderInLayer(@Nullable RenderType layer);

    BlockState getCurrentState();

    default IModelData getModelData() {
        return EmptyModelData.INSTANCE;
    }
}
