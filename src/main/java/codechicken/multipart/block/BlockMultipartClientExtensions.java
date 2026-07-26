package codechicken.multipart.block;

import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import org.jspecify.annotations.Nullable;

/**
 * Created by covers1624 on 7/13/26.
 */
public class BlockMultipartClientExtensions implements IClientBlockExtensions {

    @Override
    public boolean addHitEffects(BlockState state, Level level, @Nullable HitResult target, ParticleEngine manager) {
        if (!(target instanceof PartRayTraceResult hit)) return true;

        TileMultipart tile = BlockMultipart.getTile(level, hit.getBlockPos());
        if (tile == null) return true;

        tile.addHitEffects(hit);
        return true;
    }

    @Override
    public boolean addDestroyEffects(BlockState state, Level Level, BlockPos pos, ParticleEngine manager) {
        // Just return true, we handle this ourselves in onDestroyedByPlayer
        return true;
    }

    @Override
    public boolean playBreakSound(BlockState state, Level level, BlockPos pos) {
        // Handled in onDestroyedByPlayer
        return true;
    }
}
