package codechicken.multipart.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.model.data.ModelData;
import org.jspecify.annotations.Nullable;

/**
 * A simple {@link BlockAndTintGetter} implementation proxying the world,
 * except for the given position.
 * <p>
 * Created by covers1624 on 7/18/26.
 */
public record ProxyBlockAndTintGetter(BlockAndTintGetter other, BlockPos pos, BlockState state, ModelData modelData) implements BlockAndTintGetter {

    @Override
    public ModelData getModelData(BlockPos pos) {
        return pos.equals(this.pos) ? modelData : ModelData.EMPTY;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return pos.equals(this.pos) ? state : other.getBlockState(pos);
    }

    // @formatter:off
    @Override public float getShade(Direction face, boolean shade) { return other.getShade(face, shade); }
    @Override public LevelLightEngine getLightEngine() { return other.getLightEngine(); }
    @Override public int getBlockTint(BlockPos pos, ColorResolver resolver) { return other.getBlockTint(pos, resolver); }
    @Nullable @Override public BlockEntity getBlockEntity(BlockPos pos) { return other.getBlockEntity(pos); }
    @Override public FluidState getFluidState(BlockPos pos) { return other.getFluidState(pos); }
    @Override public int getHeight() { return other.getHeight(); }
    @Override public int getMinY() { return other.getMinY(); }
    // @formatter:on
}
