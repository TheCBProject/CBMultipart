package codechicken.multipart.api;

import codechicken.multipart.TMultiPart;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Used to convert items to parts for placement.
 * e.g, Redstone Torch
 *
 * In 1.13 this will be integrated into IPartConverter.
 * Created by covers1624 on 12/10/18.
 */
//TODO 1.13, Merge with IPartConverter.
public interface IPlacementConverter {

    /**
     * Return true if this converter can convert the ItemStack to a part.
     *
     * @param stack The stack being placed.
     * @return If conversion is possible.
     */
    boolean canConvert(ItemStack stack);

    /**
     * Convert the ItemStack to a part, then set its placement state if needed.
     *
     * @param stack   The stack being placed.
     * @param world   The world.
     * @param pos     The pos where the part will be placed.
     * @param sideHit The side of the block we hit.
     * @param hitVec  The hitVec for the block we hit.
     * @param placer  The entity placing the part.
     * @param hand    The hand being used.
     * @return The part to place.
     */
    TMultiPart convert(ItemStack stack, World world, BlockPos pos, EnumFacing sideHit, Vec3d hitVec, EntityLivingBase placer, EnumHand hand);
}
