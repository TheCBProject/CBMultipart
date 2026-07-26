package codechicken.microblock.part;

import codechicken.lib.vec.Vector3;
import codechicken.microblock.api.MicroMaterialClient;
import codechicken.multipart.api.part.render.PartParticleHandler;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.entity.Entity;

/**
 * Created by covers1624 on 7/26/26.
 */
public class MicroblockPartParticleHandler implements PartParticleHandler<MicroblockPart> {

    @Override
    public void addHitEffects(MicroblockPart part, PartRayTraceResult hit, ParticleEngine engine) {
        var clientMaterial = MicroMaterialClient.get(part.material);
        if (clientMaterial == null) return;

        clientMaterial.addHitEffects(part, hit, engine);
    }

    @Override
    public void addDestroyEffects(MicroblockPart part, PartRayTraceResult hit, ParticleEngine engine) {
        MicroMaterialClient clientMaterial = MicroMaterialClient.get(part.material);
        if (clientMaterial == null) return;

        clientMaterial.addDestroyEffects(part, hit, engine);
    }

    @Override
    public void addLandingEffects(MicroblockPart part, PartRayTraceResult hit, Vector3 entity, int numberOfParticles) {
        MicroMaterialClient clientMaterial = MicroMaterialClient.get(part.material);
        if (clientMaterial == null) return;

        clientMaterial.addLandingEffects(part, hit, entity, numberOfParticles);
    }

    @Override
    public void addRunningEffects(MicroblockPart part, PartRayTraceResult hit, Entity entity) {
        MicroMaterialClient clientMaterial = MicroMaterialClient.get(part.material);
        if (clientMaterial == null) return;

        clientMaterial.addRunningEffects(part, hit, entity);
    }
}
