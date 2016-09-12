package codechicken.multipart

import codechicken.lib.packet.PacketCustom
import codechicken.multipart.handler.MultipartCPH
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.input.Keyboard

import scala.collection.mutable.{HashMap => MHashMap}

/**
 * A class that maintains a map server<->client of which players are holding the control (or placement modifier key) much like sneaking.
 */
object ControlKeyModifer
{
    implicit def playerControlValue(p:EntityPlayer):ControlKeyValue = new ControlKeyValue(p)

    class ControlKeyValue(p:EntityPlayer)
    {
        def isControlDown = map(p)
    }

    val map = MHashMap[EntityPlayer, Boolean]().withDefaultValue(false)

    /**
     * Implicit static for Java users.
     */
    def isControlDown(p:EntityPlayer) = p.isControlDown
}

/**
 * Key Handler implementation
 */
object ControlKeyHandler extends KeyBinding("key.control", Keyboard.KEY_LCONTROL, "key.categories.gameplay")
{
    import ControlKeyModifer._
    var wasPressed = false

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    def tick(event:ClientTickEvent) {
        val pressed = isKeyDown
        if(pressed != wasPressed) {
            wasPressed = pressed
            if(Minecraft.getMinecraft.getConnection != null)
            {
                map.put(Minecraft.getMinecraft.thePlayer, pressed)
                val packet = new PacketCustom(MultipartCPH.channel, 1)
                packet.writeBoolean(pressed)
                packet.sendToServer()
            }
        }
    }
}