package codechicken.multipart

import codechicken.lib.vec.{Rotation, Vector3}
import net.minecraft.block.SoundType
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util._
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * Java class implementation
 */
abstract class JItemMultiPart extends Item with TItemMultiPart

/**
 * Simple multipart item class for easy placement. Simply override the newPart function and it the part will be added to the block space if it passes the occlusion tests.
 */
trait TItemMultiPart extends Item {
    def getHitDepth(vhit: Vector3, side: Int): Double =
        vhit.copy.scalarProject(Rotation.axes(side)) + (side % 2 ^ 1)

    override def onItemUse(player: EntityPlayer, world: World, bpos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult = {
        val stack = player.getHeldItem(hand)
        var pos = new BlockPos(bpos)
        val side = facing.getIndex
        val vhit = new Vector3(hitX, hitY, hitZ)
        val d = getHitDepth(vhit, side)

        def place(): EnumActionResult = {
            val part = newPart(stack, player, world, pos, side, vhit)
            if (part == null || !TileMultipart.canPlacePart(world, pos, part)) return EnumActionResult.FAIL

            if (!world.isRemote) {
                TileMultipart.addPart(world, pos, part)
                val sound = part.getPlacementSound(stack)
                if (sound != null) {
                    world.playSound(null, bpos, sound.getPlaceSound,
                        SoundCategory.BLOCKS, (sound.getVolume + 1.0F) / 2.0F, sound.getPitch * 0.8F)
                }
            }
            if (!player.capabilities.isCreativeMode) stack.shrink(1)
            EnumActionResult.SUCCESS
        }

        if (d < 1 && place() == EnumActionResult.SUCCESS) return EnumActionResult.SUCCESS

        pos = pos.offset(facing)
        place()
    }

    /**
     * Create a new part based on the placement information parameters.
     */
    def newPart(item: ItemStack, player: EntityPlayer, world: World, pos: BlockPos, side: Int, vhit: Vector3): TMultiPart

    /**
     * Optionally return a sound event here to have it played on a successful placement.
     */
    @Deprecated// Use TMultiPart.getPlacementSound.
    def getPlacementSound(item: ItemStack): SoundType = null
}
