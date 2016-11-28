package codechicken.microblock.handler

import codechicken.lib.render.RenderUtils
import codechicken.microblock.{ItemMicroPartRenderer, MicroMaterialRegistry}
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.client.event.{DrawBlockHighlightEvent, TextureStitchEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.opengl.GL11

object MicroblockEventHandler
{
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    def postTextureStitch(event:TextureStitchEvent.Post)
    {
        MicroMaterialRegistry.markIconReload()
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    def drawBlockHighlight(event:DrawBlockHighlightEvent)
    {
        val currentItem = event.getPlayer.getHeldItemMainhand

        if(currentItem != null && currentItem.getItem == MicroblockProxy.itemMicro &&
                event.getTarget != null && event.getTarget.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            GL11.glPushMatrix()
                RenderUtils.translateToWorldCoords(event.getPlayer, event.getPartialTicks)
                if(ItemMicroPartRenderer.renderHighlight(event.getPlayer, currentItem, event.getTarget))
                    event.setCanceled(true)
            GL11.glPopMatrix()
        }
    }
}