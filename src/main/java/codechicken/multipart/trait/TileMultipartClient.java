package codechicken.multipart.trait;

import codechicken.lib.vec.Vector3;
import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.client.MultipartModelData;
import codechicken.multipart.util.PartRayTraceResult;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

/**
 * Manual trait implemented on every client side TileMultiPart.
 */
public class TileMultipartClient extends TileMultipart {

    public TileMultipartClient(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public boolean isClientTile() {
        return true;
    }

    @Override
    public void markRender() {
        if (getLevel() instanceof ClientLevel) {
            requestModelDataUpdate();
            BlockPos pos = getBlockPos();
            Minecraft.getInstance().levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
        }
    }

    @Override
    public ModelData getModelData() {
        var parts = getPartList();
        var partData = ImmutableList.<MultipartModelData.PartModelData>builderWithExpectedSize(parts.size());
        for (MultiPart part : parts) {
            partData.add(new MultipartModelData.PartModelData(
                    part.getType(),
                    part.getModelData()
            ));
        }
        return ModelData.of(
                MultipartModelData.DATA,
                new MultipartModelData(
                        tile(),
                        partData.build()
                )
        );
    }

    @Override
    public void addHitEffects(PartRayTraceResult hit) {
        var part = hit.part;
        var handler = MultipartClientRegistry.getPartParticleHandler(part.getType());
        if (handler == null) return;

        handler.addHitEffects(part, hit, Minecraft.getInstance().particleEngine);
    }

    @Override
    public void addDestroyEffects(PartRayTraceResult hit) {
        var part = hit.part;
        var handler = MultipartClientRegistry.getPartParticleHandler(part.getType());
        if (handler == null) return;

        handler.addDestroyEffects(part, hit, Minecraft.getInstance().particleEngine);
    }

    @Override
    public void addLandingEffects(Vector3 entity, int numberOfParticles) {
        if (!(hitFeet(entity) instanceof PartRayTraceResult hit)) return;

        var part = hit.part;
        var handler = MultipartClientRegistry.getPartParticleHandler(part.getType());
        if (handler == null) return;

        handler.addLandingEffects(part, hit, entity, numberOfParticles);
    }

    @Override
    public void addRunningEffects(Entity entity) {
        if (!(hitFeet(Vector3.fromEntity(entity)) instanceof PartRayTraceResult hit)) return;

        var part = hit.part;
        var handler = MultipartClientRegistry.getPartParticleHandler(part.getType());
        if (handler == null) return;

        handler.addRunningEffects(part, hit, entity);
    }
}
