package codechicken.microblock.api;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.part.MicroblockPart;
import codechicken.microblock.util.MaskedCuboid;
import codechicken.multipart.util.PartRayTraceResult;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
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

    public void renderDynamic(MicroblockPart part, @Nullable ItemTransforms.TransformType transformType, PoseStack pStack, MultiBufferSource buffers, int packedLight, int packedOverlay, float partialTicks) { }

    public void addHitEffects(MicroblockPart part, PartRayTraceResult hit, ParticleEngine engine) { }

    public void addDestroyEffects(MicroblockPart part, PartRayTraceResult hit, ParticleEngine engine) { }

    public void addLandingEffects(MicroblockPart part, PartRayTraceResult hit, Vector3 entity, int numberOfParticles) { }

    public void addRunningEffects(MicroblockPart part, PartRayTraceResult hit, Entity entity) { }
}
