package codechicken.microblock.api;

import codechicken.lib.render.CCRenderState;
import codechicken.microblock.util.MaskedCuboid;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 23/10/22.
 */
public abstract class MicroMaterialClient {

    @Nullable
    public static MicroMaterialClient get(MicroMaterial material) {
        return (MicroMaterialClient) material.renderProperties;
    }

    public abstract RenderType getItemRenderLayer();

    public abstract boolean renderCuboids(CCRenderState ccrs, @Nullable RenderType layer, Iterable<MaskedCuboid> cuboids);
}
