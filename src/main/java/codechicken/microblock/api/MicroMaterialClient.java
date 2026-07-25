package codechicken.microblock.api;

import codechicken.lib.vec.Vector3;
import codechicken.microblock.part.MicroblockPart;
import codechicken.microblock.util.MaskedCuboid;
import codechicken.multipart.util.PartRayTraceResult;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by covers1624 on 23/10/22.
 */
public abstract class MicroMaterialClient {

    @Nullable
    public static MicroMaterialClient get(MicroMaterial material) {
        return (MicroMaterialClient) material.renderProperties;
    }

    /**
     * Collect the model parts of this material.
     *
     * @param level         The level, {@code null} when in inventory, otherwise a view from the chunk batching thread.
     * @param pos           The position in world, {@code null} when in inventory.
     * @param renderCuboids The cuboids and their masks we wish to render.
     * @param parts         The parts.
     */
    public abstract void collectParts(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, ImmutableSet<MaskedCuboid> renderCuboids, List<BlockModelPart> parts);

    // TODO
//    public void submitDynamic(MicroblockPart part, ItemDisplayContext displayCtx, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int packedOverlay, boolean hasFoil, int outlineColor) { }

    public void addHitEffects(MicroblockPart part, PartRayTraceResult hit, ParticleEngine engine) { }

    public void addDestroyEffects(MicroblockPart part, PartRayTraceResult hit, ParticleEngine engine) { }

    public void addLandingEffects(MicroblockPart part, PartRayTraceResult hit, Vector3 entity, int numberOfParticles) { }

    public void addRunningEffects(MicroblockPart part, PartRayTraceResult hit, Entity entity) { }
}
