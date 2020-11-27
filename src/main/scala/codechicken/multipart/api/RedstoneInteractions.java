package codechicken.multipart.api;

import codechicken.lib.vec.Rotation;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.redstone.IFaceRedstonePart;
import codechicken.multipart.api.part.redstone.IMaskedRedstonePart;
import codechicken.multipart.api.part.redstone.IRedstonePart;
import codechicken.multipart.api.tile.IRedstoneConnector;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import java.util.Set;

/**
 * Created by covers1624 on 21/10/20.
 */
public class RedstoneInteractions {

    private static final Set<Block> FULL_VANILLA_BLOCKS = ImmutableSet.<Block>builder()//
            .add(Blocks.REDSTONE_TORCH)//
            .add(Blocks.REDSTONE_WALL_TORCH)//
            .add(Blocks.LEVER)//
            .add(Blocks.STONE_BUTTON)//
            .add(Blocks.BIRCH_BUTTON)//
            .add(Blocks.ACACIA_BUTTON)//
            .add(Blocks.DARK_OAK_BUTTON)//
            .add(Blocks.JUNGLE_BUTTON)//
            .add(Blocks.OAK_BUTTON)//
            .add(Blocks.SPRUCE_BUTTON)//
            .add(Blocks.REDSTONE_BLOCK)//
            .build();

    public static int connectionMask(TMultiPart p, int side) {
        if (p instanceof IRedstonePart && ((IRedstonePart) p).canConnectRedstone(side)) {
            if (p instanceof IFaceRedstonePart) {
                int fside = ((IFaceRedstonePart) p).getFace();
                if ((side & 6) == (fside & 6)) {
                    return 0x10;
                }

                return 1 << Rotation.rotationTo(side & 6, fside);
            } else if (p instanceof IMaskedRedstonePart) {
                return ((IMaskedRedstonePart) p).getConnectionMask(side);
            }
            return 0x1F;
        }
        return 0;
    }

    public static int getConnectionMask(IWorldReader world, BlockPos pos, int side, boolean power) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IRedstoneConnector) {
            return ((IRedstoneConnector) tile).getConnectionMask(side);
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IRedstoneConnectorBlock) {
            ((IRedstoneConnectorBlock) block).getConnectionMask(world, pos, side);
        }
        return vanillaConnectionMask(world, pos, state, side, power);
    }

    public static int vanillaConnectionMask(IWorldReader world, BlockPos pos, BlockState state, int side, boolean power) {
        Block block = state.getBlock();
        if (FULL_VANILLA_BLOCKS.contains(block)) {
            return 0x1F;
        }

        if (side == 0) { //vanilla doesn't handle side 0
            return power ? 0x1F : 0;
        }

        /*
         * so that these can be conducted to from face parts on the other side of the block.
         * Due to vanilla's inadequecy with redstone/logic on walls
         */
        if (block == Blocks.REDSTONE_WIRE || block == Blocks.COMPARATOR) {
            if (side != 0) {
                return power ? 0x1F : 4;
            }
            return 0;
        }

        if (block == Blocks.REPEATER) { //stupid minecraft hardcodes
            int fside = state.get(HorizontalBlock.HORIZONTAL_FACING).ordinal();
            if ((side & 6) == (fside & 6)) {
                return power ? 0x1F : 4;
            }
            return 0;
        }
        if (power || block.canConnectRedstone(state, world, pos, Direction.byIndex(side))) {
            return 0x1F;
        }
        return 0;
    }

}
