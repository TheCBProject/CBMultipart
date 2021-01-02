package codechicken.multipart.api;

import codechicken.lib.vec.Rotation;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.redstone.IFaceRedstonePart;
import codechicken.multipart.api.part.redstone.IMaskedRedstonePart;
import codechicken.multipart.api.part.redstone.IRedstonePart;
import codechicken.multipart.api.tile.IRedstoneConnector;
import codechicken.multipart.trait.extern.IRedstoneTile;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.Set;

/**
 * static helper class for calculating various things about redstone.
 * Indirect power (also known as strong power) is not handled here, just use world.getIndirectPowerTo
 * Masks are defined in IRedstoneConnector
 */
public class RedstoneInteractions {

    /**
     * Hardcoded vanilla overrides for Block.canConnectRedstone {@link IRedstoneConnectorBlock}
     */
    private static final Set<Block> FULL_VANILLA_BLOCKS = ImmutableSet.<Block>builder()
            .add(Blocks.REDSTONE_TORCH)
            .add(Blocks.REDSTONE_WALL_TORCH)
            .add(Blocks.LEVER)
            .add(Blocks.STONE_BUTTON)
            .add(Blocks.BIRCH_BUTTON)
            .add(Blocks.ACACIA_BUTTON)
            .add(Blocks.DARK_OAK_BUTTON)
            .add(Blocks.JUNGLE_BUTTON)
            .add(Blocks.OAK_BUTTON)
            .add(Blocks.SPRUCE_BUTTON)
            .add(Blocks.REDSTONE_BLOCK)
            .add(Blocks.REDSTONE_LAMP)
            .build();

    /**
     * Get the direct power to p on side
     */
    public static int getPowerTo(TMultiPart p, int side) {
        int oc = ((IRedstoneTile) p.tile()).openConnections(side);
        return getPowerTo(p.world(), p.pos(), side, oc & connectionMask(p, side));
    }

    /**
     * Get the direct power level to space (pos) on side with mask
     */
    public static int getPowerTo(World world, BlockPos pos, int side, int mask) {
        return getPower(world, pos.offset(Direction.byIndex(side)), side ^ 1, mask);
    }

    /**
     * Get the direct power level provided by space (pos) on side with mask
     */
    public static int getPower(World world, BlockPos pos, int side, int mask) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IRedstoneConnector) {
            return ((IRedstoneConnector) tile).weakPowerLevel(side, mask);
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IRedstoneConnectorBlock) {
            return ((IRedstoneConnectorBlock) block).weakPowerLevel(world, pos, side, mask);
        }

        int vMask = vanillaConnectionMask(world, pos, state, side, true);
        if ((vMask & mask) > 0) {
            int m = world.getRedstonePower(pos, Direction.byIndex(side ^ 1));
            if (m < 15 && block == Blocks.REDSTONE_WIRE) {
                m = Math.max(m, state.get(RedstoneWireBlock.POWER));
            } //painful vanilla kludge
            return m;
        }
        return 0;
    }

    /**
     * Get the connection mask of the block on side of (pos).
     *
     * @param power , whether the connection mask is for signal transfer or visual connection. (some blocks accept power without visual connection)
     */
    public static int otherConnectionMask(IWorldReader world, BlockPos pos, int side, boolean power) {
        return getConnectionMask(world, pos.offset(Direction.byIndex(side)), side ^ 1, power);
    }

    /**
     * Get the connection mask of part on side
     */
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

    /**
     * @param power If true, don't test canConnectRedstone on blocks, just get a power transmission mask rather than a visual connection
     */
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

    /**
     * Returns the connection mask for a vanilla block
     */
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
