package codechicken.multipart.api;

import codechicken.multipart.api.tile.IRedstoneConnector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;

/**
 * Block version of {@link IRedstoneConnector}
 * Due to the inadequate {@link Block#canConnectRedstone} not handling the bottom side (nor the top particularly well)
 */
public interface IRedstoneConnectorBlock {

    int getConnectionMask(LevelReader world, BlockPos pos, int side);

    int weakPowerLevel(LevelReader world, BlockPos pos, int side, int mask);
}
