package codechicken.multipart.api;

import codechicken.multipart.TMultiPart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;

/**
 * Interface for converting existing blocks/tiles to multipart versions.
 */
public interface IPartConverter {

    /**
     * Return true if this converter can convert the provided block/tile to a Multipart.
     *
     * @param world The world.
     * @param pos   The position in the world to attempt conversion.
     * @param state The state of the block in world.
     * @return If conversion is possible.
     */
    boolean canConvert(World world, BlockPos pos, IBlockState state);

    /**
     * Convert the block/tile in world to a List of TMultiParts.
     * By default this delegates to {@link #convert}
     *
     * @param world The world.
     * @param pos   The position in world to attempt conversion.
     * @param state The state of the block in world.
     * @return An Iterable of parts, Empty if no conversion was possible.
     */
    default Iterable<TMultiPart> convertToParts(World world, BlockPos pos, IBlockState state) {
        TMultiPart part = convert(world, pos, state);
        if (part != null) {
            return Collections.singleton(part);
        }
        return Collections.emptySet();
    }

    /**
     * Converts the block/tile in world to a single TMultiPart.
     * If your block/tile is essentially Multiple parts, use {@link #convertToParts}
     * It is safe to ignore this method if you are using {@link #convertToParts}
     *
     * @param world The world.
     * @param pos   The position in world to attempt conversion.
     * @param state The state of the block in world.
     * @return The part the block/tile was converted to. Null to delete the part.
     */
    TMultiPart convert(World world, BlockPos pos, IBlockState state);
}
