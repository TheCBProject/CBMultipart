package codechicken.microblock.api;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.part.MicroblockPart;
import codechicken.microblock.util.MaskedCuboid;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
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

    public void addHitEffects(MicroblockPart part, PartRayTraceResult hit, ParticleEngine engine) { }

    public void addDestroyEffects(MicroblockPart part, PartRayTraceResult hit, ParticleEngine engine) { }

    public void addLandingEffects(MicroblockPart part, PartRayTraceResult hit, Vector3 entity, int numberOfParticles) { }

    public void addRunningEffects(MicroblockPart part, PartRayTraceResult hit, Entity entity) { }
}
