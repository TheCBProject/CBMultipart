package codechicken.multipart.api;

import codechicken.lib.vec.Rotation;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.redstone.FaceRedstonePart;
import codechicken.multipart.api.part.redstone.MaskedRedstonePart;
import codechicken.multipart.api.part.redstone.RedstonePart;
import codechicken.multipart.api.tile.RedstoneConnector;
import codechicken.multipart.trait.extern.RedstoneTile;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/**
 * static helper class for calculating various things about redstone.
 * Indirect power (also known as strong power) is not handled here, just use world.getIndirectPowerTo
 * Masks are defined in IRedstoneConnector
 */
public class RedstoneInteractions {

    /**
     * Hardcoded vanilla overrides for Block.canConnectRedstone {@link RedstoneConnectorBlock}
     */
    private static final Set<Block> FULL_VANILLA_BLOCKS = ImmutableSet.<Block>builder()
            .add(Blocks.REDSTONE_LAMP)
            .build();

    /**
     * Get the direct power to p on side
     */
    public static int getPowerTo(MultiPart p, int side) {
        int oc = ((RedstoneTile) p.tile()).openConnections(side);
        return getPowerTo(p.level(), p.pos(), side, oc & connectionMask(p, side));
    }

    /**
     * Get the direct power level to space (pos) on side with mask
     */
    public static int getPowerTo(Level world, BlockPos pos, int side, int mask) {
        return getPower(world, pos.relative(Direction.from3DDataValue(side)), side ^ 1, mask);
    }

    /**
     * Get the direct power level provided by space (pos) on side with mask
     */
    public static int getPower(Level world, BlockPos pos, int side, int mask) {
        if (world.getBlockEntity(pos) instanceof RedstoneConnector cond) {
            return cond.weakPowerLevel(side, mask);
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof RedstoneConnectorBlock bl) {
            return bl.weakPowerLevel(world, pos, side, mask);
        }

        int vMask = vanillaConnectionMask(world, pos, state, side, true);
        if ((vMask & mask) > 0) {
            int m = world.getSignal(pos, Direction.from3DDataValue(side ^ 1));
            if (m < 15 && block == Blocks.REDSTONE_WIRE) {
                m = Math.max(m, state.getValue(RedStoneWireBlock.POWER));
            } //painful vanilla kludge
            return m;
        }
        return 0;
    }

    /**
     * Get the connection mask of the block on side of (pos). See {@link #getConnectionMask(LevelReader, BlockPos, int, boolean)}
     *
     * @param world The world
     * @param pos   The position of the original block
     * @param side  The side the target block is in that will be queried
     * @param power If true, returns a power transmission mask rather than visual connection mask
     */
    public static int otherConnectionMask(LevelReader world, BlockPos pos, int side, boolean power) {
        return getConnectionMask(world, pos.relative(Direction.from3DDataValue(side)), side ^ 1, power);
    }

    /**
     * Get the connection mask of part on a given block side.
     *
     * @param p The part to query
     * @param side The side to query, relative to the part's parent block
     */
    public static int connectionMask(MultiPart p, int side) {
        if (p instanceof RedstonePart && ((RedstonePart) p).canConnectRedstone(side)) {
            if (p instanceof FaceRedstonePart part) {
                int fside = part.getFace();
                if ((side & 6) == (fside & 6)) {
                    return 0x10;
                }

                return 1 << Rotation.rotationTo(side & 6, fside);
            }
            if (p instanceof MaskedRedstonePart part) {
                return part.getConnectionMask(side);
            }
            return 0x1F;
        }
        return 0;
    }

    /**
     * Retrieves connection mask for block at give pos. If the block/entity implements the more capable RedstoneConnector/RedstoneConnectorBlock
     * interfaces, it provides the mask directly. Otherwise, it is calculated from known vanilla blocks. For unknown blocks, the default canConnectRedstone
     * logic applies.
     * <p>
     * If power is true, the power transmission mask is returned instead. Some blocks can accept/emit signal even without a visible connection.
     *
     * @param world The level
     * @param pos   The position of the block to query
     * @param side  The side of the block to query
     * @param power If true, returns a power transmission mask rather than visual connection mask
     */
    public static int getConnectionMask(LevelReader world, BlockPos pos, int side, boolean power) {
        if (world.getBlockEntity(pos) instanceof RedstoneConnector cond) {
            return cond.getConnectionMask(side);
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof RedstoneConnectorBlock bl) {
            return bl.getConnectionMask(world, pos, side);
        }
        return vanillaConnectionMask(world, pos, state, side, power);
    }

    /**
     * Calculates the connection mask for a vanilla block. This will take care of calculating partial masks for non-full blocks,
     * which default block methods cannot handle.
     * <p>
     * If power is true, the power transmission mask is returned instead. Some blocks can accept/emit signal even without a visible connection.
     *
     * @param world The level
     * @param pos   The position of the vanilla block
     * @param state The state of the vanilla block
     * @param side  The side of the vanilla block to query
     * @param power If true, returns a power transmission mask rather than visual connection mask
     */
    public static int vanillaConnectionMask(LevelReader world, BlockPos pos, BlockState state, int side, boolean power) {
        Block block = state.getBlock();
        if (FULL_VANILLA_BLOCKS.contains(block)) {
            return 0x1F;
        }

        /*
         * Manual overrides for FaceRedstonePart-like blocks, which can connect:
         *  - on the 4 edges about the affixed face
         *  - through the center on or opposite the affixed face
         */

        // Dust
        if (block == Blocks.REDSTONE_WIRE) {
            if (side == 1) return 0;
            return power ? 0x1F : 4;
        }

        // Comparator
        if (block == Blocks.COMPARATOR) {
            if (side == 0 || side == 1) return 0;
            return power ? 0x1F : 4;
        }

        // Repeaters
        if (block == Blocks.REPEATER) { //stupid minecraft hardcodes
            int fside = state.getValue(HorizontalDirectionalBlock.FACING).ordinal();
            if ((side & 6) == (fside & 6)) {
                return power ? 0x1F : 4;
            }
            return 0;
        }

        // Standing torches
        if (block == Blocks.REDSTONE_TORCH) {
            if (power) return 0x1F;

            if (side == 0 || side == 1) { // Top or bottom face
                return 0x10;
            }
            // Edge touching side 0
            return 4;
        }

        // Wall torches
        if (block == Blocks.REDSTONE_WALL_TORCH) {
            if (power) return 0x1F;

            int fside = state.getValue(RedstoneWallTorchBlock.FACING).getOpposite().ordinal();
            if ((side & 6) == (fside & 6)) {
                return 0x10;
            }

            // Edge between attached face and queried face
            return 1 << Rotation.rotationTo(side & 6, fside);
        }

        // Buttons and levers
        if (block instanceof ButtonBlock || block instanceof LeverBlock) {
            if (power) return 0x1F;

            int fside = FaceAttachedHorizontalDirectionalBlock.getConnectedDirection(state).getOpposite().ordinal();
            if ((side & 6) == (fside & 6)) {
                return 0x10;
            }

            // Edge between attached face and queried face
            return 1 << Rotation.rotationTo(side & 6, fside);
        }

        // Observer
        if (block == Blocks.OBSERVER) {
            int fside = state.getValue(ObserverBlock.FACING).ordinal();
            if (side == (fside ^ 1)) {
                return 0x1F; // Full backface
            }
            return 0; // Only backside connects/emits
        }

        // For all other blocks, rely on canConnectRedstone logic. Side is respective to the wire
        // attempting the connection, not the target side. Therefore, it is flipped
        if (power || block.canConnectRedstone(state, world, pos, Direction.from3DDataValue(side).getOpposite())) {
            return 0x1F;
        }
        return 0;
    }

}
