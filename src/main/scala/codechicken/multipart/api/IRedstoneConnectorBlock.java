package codechicken.multipart.api;

import codechicken.multipart.api.tile.IRedstoneConnector;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

/**
 * Block version of {@link IRedstoneConnector}
 * Due to the inadequate {@link Block#canConnectRedstone} not handling the bottom side (nor the top particularly well)
 */
public interface IRedstoneConnectorBlock {

    int getConnectionMask(IWorldReader world, BlockPos pos, int side);

    int weakPowerLevel(IWorldReader world, BlockPos pos, int side, int mask);
}
