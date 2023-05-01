package codechicken.microblock.api;

import codechicken.lib.render.CCRenderState;
import codechicken.microblock.part.MicroblockPart;
import codechicken.microblock.util.MaskedCuboid;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.client.particle.ParticleEngine;
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

    public abstract void addHitEffects(MicroblockPart part, PartRayTraceResult hit, ParticleEngine engine);

    public abstract void addDestroyEffects(MicroblockPart part, PartRayTraceResult hit, ParticleEngine engine);
}
