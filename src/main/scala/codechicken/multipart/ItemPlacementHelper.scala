package codechicken.multipart

import codechicken.lib.packet.PacketCustom
import codechicken.lib.raytracer.RayTracer
import codechicken.multipart.handler.MultipartSPH
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.{EnumHand, SoundCategory}
import net.minecraft.world.World
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.{EventPriority, SubscribeEvent}

/**
 * Created by covers1624 on 12/10/18.
 */
object ItemPlacementHelper {

    //interaction lock.
    private val placing = new ThreadLocal[Any]

    @SubscribeEvent(priority = EventPriority.LOW)
    def playerInteract(event: PlayerInteractEvent.RightClickBlock) {
        if (event.getWorld.isRemote) {
            if (placing.get() != null) {
                return
            }
            placing.set(event)
            if (place(event.getEntityPlayer, event.getHand, event.getWorld)) {
                event.setCanceled(true)
            }
            placing.set(null)
        }
    }

    def place(player: EntityPlayer, hand: EnumHand, world: World): Boolean = {
        val held = player.getHeldItem(hand)
        if (held.isEmpty) {
            return false
        }
        val hit = RayTracer.retrace(player)
        if (hit == null) {
            return false
        }

        val pos = hit.getBlockPos.offset(hit.sideHit)
        val part = MultiPartRegistry.convertItem(held, world, pos, hit.sideHit, hit.hitVec, player, hand)

        if (part == null) {
            return false
        }
        val tile = TileMultipart.getOrConvertTile(world, pos)
        if (tile == null || !tile.canAddPart(part)) {
            return false
        }

        if (!world.isRemote) {
            TileMultipart.addPart(world, pos, part)
            val sound = part.getPlacementSound(held)
            if (sound != null) {
                world.playSound(null, pos.getX + 0.5D, pos.getY + 0.5D, pos.getZ + 0.5D, sound.getPlaceSound, SoundCategory.BLOCKS, (sound.getVolume + 1.0F) / 2.0F, sound.getPitch * 0.8F)
            }
            if (!player.capabilities.isCreativeMode) {
                held.shrink(1)
                if (held.isEmpty) {
                    ForgeEventFactory.onPlayerDestroyItem(player, held, hand)
                }
            }
        } else {
            player.swingArm(hand)
            val packet = new PacketCustom(MultipartSPH.channel, 10)
            packet.writeBoolean(hand == EnumHand.MAIN_HAND)
            packet.sendToServer()
        }
        true
    }
}
