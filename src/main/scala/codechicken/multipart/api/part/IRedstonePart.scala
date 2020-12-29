package codechicken.multipart.api.part

import codechicken.lib.vec.Rotation._
import codechicken.multipart.`trait`.extern.IRedstoneTile
import codechicken.multipart.api.IRedstoneConnectorBlock
import codechicken.multipart.api.part.redstone.{IFaceRedstonePart, IMaskedRedstonePart, IRedstonePart}
import codechicken.multipart.api.tile.IRedstoneConnector
import net.minecraft.block._
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{IWorldReader, World}

///**
// * Interface for parts with redstone interaction
// *
// * Marker interface for TRedstoneTile. This means that if a part is an instance of IRedstonePart, the container tile may be cast to TRedstoneTile
// */
//trait IRedstonePart {
//    /**
//     * Returns the strong (indirect, through blocks) signal being emitted by this part to the specified side
//     */
//    def strongPowerLevel(side: Int): Int
//
//    /**
//     * Returns the weak (direct) signal being emitted by this part to the specified side
//     */
//    def weakPowerLevel(side: Int): Int
//
//    /**
//     * Returns true if this part can connect to redstone on the specified side. Blocking parts like covers will be handled by RedstoneInteractions
//     */
//    def canConnectRedstone(side: Int): Boolean
//}
//
///**
// * For parts like wires that adhere to a specific face, reduces redstone connections to the specific edge between two faces.
// * Should be implemented on parts implementing TFacePart
// */
//trait IFaceRedstonePart extends IRedstonePart {
//    /**
//     * Return the face to which this redstone part is attached
//     */
//    def getFace: Int
//}
//
///**
// * For parts that want to define their own connection masks (like center-center parts)
// */
//trait IMaskedRedstonePart extends IRedstonePart {
//    /**
//     * Returns the redstone connection mask for this part on side. See IRedstoneConnector for mask definition
//     */
//    def getConnectionMask(side: Int): Int
//}
//
///**
// * Interface for tile entities which split their redstone connections into a mask for each side (edges and center)
// *
// * All connection masks are a 5 bit map.
// * The lowest 4 bits correspond to the connection toward the face specified Rotation.rotateSide(side&6, b) where b is the bit index from lowest to highest.
// * Bit 5 corresponds to a connection opposite side.
// */
//trait IRedstoneConnector {
//    /**
//     * Returns the redstone connection mask for this tile on side.
//     */
//    def getConnectionMask(side: Int): Int
//
//    /**
//     * Returns the weak power level provided by this tile on side through mask
//     */
//    def weakPowerLevel(side: Int, mask: Int): Int
//}
//
///**
// * Internal interface for TileMultipart instances hosting IRedstonePart
// */
//trait IRedstoneTile extends IRedstoneConnector {
//    /**
//     * Returns a mask of spaces through which a wire could connect on side
//     */
//    def openConnections(side: Int): Int
//}
//
///**
// * Block version of IRedstoneConnector
// * Due to the inadequate Block.canConnectRedstone not handling the bottom side (nor the top particularly well)
// */
//trait IRedstoneConnectorBlock {
//    def getConnectionMask(world: IWorldReader, pos: BlockPos, side: Int): Int
//
//    def weakPowerLevel(world: IWorldReader, pos: BlockPos, side: Int, mask: Int): Int
//}

/**
 * static helper class for calculating various things about redstone.
 * Indirect power (also known as strong power) is not handled here, just use world.getIndirectPowerTo
 * Masks are defined in IRedstoneConnector
 */
object RedstoneInteractions {
    /**
     * Hardcoded vanilla overrides for Block.canConnectRedstone (see @IRedstoneConnectorBlock)
     */
    val fullVanillaBlocks = Set(
        Blocks.REDSTONE_TORCH,
        Blocks.REDSTONE_WALL_TORCH,
        Blocks.LEVER,
        Blocks.STONE_BUTTON,
        Blocks.BIRCH_BUTTON,
        Blocks.ACACIA_BUTTON,
        Blocks.DARK_OAK_BUTTON,
        Blocks.JUNGLE_BUTTON,
        Blocks.OAK_BUTTON,
        Blocks.SPRUCE_BUTTON,
        Blocks.REDSTONE_BLOCK,
        Blocks.REDSTONE_LAMP
    )

    /**
     * Get the direct power to p on side
     */
    def getPowerTo(p: TMultiPart, side: Int): Int = {
        val tile = p.tile
        getPowerTo(tile.getWorld, tile.getPos, side,
            tile.asInstanceOf[IRedstoneTile].openConnections(side) & connectionMask(p, side))
    }

    /**
     * Get the direct power level to space (x, y, z) on side with mask
     */
    def getPowerTo(world: World, pos: BlockPos, side: Int, mask: Int): Int =
        getPower(world, pos.offset(Direction.BY_INDEX(side)), side ^ 1, mask)


    /**
     * Get the direct power level provided by space (x, y, z) on side with mask
     */
    def getPower(world: World, pos: BlockPos, side: Int, mask: Int): Int = {
        val tile = world.getTileEntity(pos)
        if (tile.isInstanceOf[IRedstoneConnector]) {
            return tile.asInstanceOf[IRedstoneConnector].weakPowerLevel(side, mask)
        }

        val state = world.getBlockState(pos)
        val block = state.getBlock
        if (block.isInstanceOf[IRedstoneConnectorBlock]) {
            return block.asInstanceOf[IRedstoneConnectorBlock].weakPowerLevel(world, pos, side, mask)
        }

        val vmask = vanillaConnectionMask(state, world, pos, side, true)
        if ((vmask & mask) > 0) {
            var m = world.getRedstonePower(pos, Direction.BY_INDEX(side ^ 1))
            if (m < 15 && block == Blocks.REDSTONE_WIRE) {
                m = math.max(m, state.get(RedstoneWireBlock.POWER))
            } //painful vanilla kludge
            return m
        }
        return 0
    }

    //def vanillaToSide(vside:Int) = sideVanillaMap(vside+1)

    /**
     * Get the connection mask of the block on side of (x, y, z).
     *
     * @param power , whether the connection mask is for signal transfer or visual connection. (some blocks accept power without visual connection)
     */
    def otherConnectionMask(world: IWorldReader, pos: BlockPos, side: Int, power: Boolean): Int =
        getConnectionMask(world, pos.offset(Direction.BY_INDEX(side)), side ^ 1, power)

    /**
     * Get the connection mask of part on side
     */
    def connectionMask(p: TMultiPart, side: Int): Int = {
        if (p.isInstanceOf[IRedstonePart] && p.asInstanceOf[IRedstonePart].canConnectRedstone(side)) {
            if (p.isInstanceOf[IFaceRedstonePart]) {
                val fside = p.asInstanceOf[IFaceRedstonePart].getFace
                if ((side & 6) == (fside & 6)) {
                    return 0x10
                }

                return 1 << rotationTo(side & 6, fside)
            }
            else if (p.isInstanceOf[IMaskedRedstonePart]) {
                return p.asInstanceOf[IMaskedRedstonePart].getConnectionMask(side)
            }
            return 0x1F
        }
        0
    }

    /**
     * @param power If true, don't test canConnectRedstone on blocks, just get a power transmission mask rather than a visual connection
     */
    def getConnectionMask(world: IWorldReader, pos: BlockPos, side: Int, power: Boolean): Int = {
        val tile = world.getTileEntity(pos)
        if (tile.isInstanceOf[IRedstoneConnector]) {
            return tile.asInstanceOf[IRedstoneConnector].getConnectionMask(side)
        }

        val state = world.getBlockState(pos)
        val block = state.getBlock
        if (block.isInstanceOf[IRedstoneConnectorBlock]) {
            return block.asInstanceOf[IRedstoneConnectorBlock].getConnectionMask(world, pos, side)
        }

        vanillaConnectionMask(state, world, pos, side, power)
    }

    /**
     * Returns the connection mask for a vanilla block
     */
    def vanillaConnectionMask(state: BlockState, world: IWorldReader, pos: BlockPos, side: Int, power: Boolean): Int = {
        val block = state.getBlock

        if (fullVanillaBlocks(block)) {
            return 0x1F
        }

        if (side == 0) //vanilla doesn't handle side 0
        {
            return if (power) 0x1F else 0
        }

        /**
         * so that these can be conducted to from face parts on the other side of the block.
         * Due to vanilla's inadequecy with redstone/logic on walls
         */
        if (block == Blocks.REDSTONE_WIRE || block == Blocks.COMPARATOR) {
            if (side != 0) {
                return if (power) 0x1F else 4
            }

            return 0
        }

        if (block == Blocks.REPEATER) //stupid minecraft hardcodes
        {
            val fside = state.get(HorizontalBlock.HORIZONTAL_FACING).ordinal
            if ((side & 6) == (fside & 6)) {
                return if (power) 0x1F else 4
            }

            return 0
        }

        if (power || block.canConnectRedstone(state, world, pos, Direction.BY_INDEX(side))) //some blocks accept power without visualising connections
        {
            return 0x1F
        }

        0
    }
}
