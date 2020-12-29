package codechicken.multipart

import codechicken.lib.vec.{Rotation, Vector3}
import codechicken.multipart.api.part.TMultiPart
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.{Item, ItemStack, ItemUseContext}
import net.minecraft.util._
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * Java class implementation
 */
abstract class JItemMultiPart(properties: Item.Properties) extends Item(properties) with TItemMultiPart

/**
 * Simple multipart item class for easy placement. Simply override the newPart function and it the part will be added to the block space if it passes the occlusion tests.
 */
trait TItemMultiPart extends Item {
    def getHitDepth(vhit: Vector3, side: Int): Double =
        vhit.copy.scalarProject(Rotation.axes(side)) + (side % 2 ^ 1)

    override def onItemUse(context: ItemUseContext): ActionResultType = {
        val stack = context.getPlayer.getHeldItem(context.getHand)
        val world = context.getWorld
        val player = context.getPlayer
        var pos = context.getPos
        val side = context.getFace.getIndex
        val vhit = new Vector3(context.getHitVec).subtract(pos)
        val d = getHitDepth(vhit, side)

        def place(offset: Boolean): ActionResultType = {
            val part = newPart(stack, player, world, pos, side, vhit)
            if (part == null || !TileMultipart.canPlacePart(context, part, offset)) return ActionResultType.FAIL

            if (!world.isRemote) {
                TileMultipart.addPart(world, pos, part)
                val sound = part.getPlacementSound(stack, player)
                if (sound != null) {
                    world.playSound(null, pos, sound.getPlaceSound,
                        SoundCategory.BLOCKS, (sound.getVolume + 1.0F) / 2.0F, sound.getPitch * 0.8F)
                }
            }
            if (!player.abilities.isCreativeMode) stack.shrink(1)
            ActionResultType.SUCCESS
        }

        if (d < 1 && place(false) == ActionResultType.SUCCESS) return ActionResultType.SUCCESS

        pos = pos.offset(context.getFace)
        place(true)
    }

    /**
     * Create a new part based on the placement information parameters.
     */
    def newPart(item: ItemStack, player: PlayerEntity, world: World, pos: BlockPos, side: Int, vhit: Vector3): TMultiPart
}
