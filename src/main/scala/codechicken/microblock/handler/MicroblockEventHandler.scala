package codechicken.microblock.handler

import codechicken.lib.render.RenderUtils
import codechicken.microblock.{ItemMicroPartRenderer, MicroMaterialRegistry}
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.client.event.{DrawBlockHighlightEvent, TextureStitchEvent}
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.{EventPriority, SubscribeEvent}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object MicroblockEventHandler {
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    def postTextureStitch(event: TextureStitchEvent.Post) {
        MicroMaterialRegistry.markIconReload()
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    @SideOnly(Side.CLIENT)
    def drawBlockHighlight(event: DrawBlockHighlightEvent) {
        val currentItem = event.getPlayer.getHeldItemMainhand

        if (!currentItem.isEmpty && currentItem.getItem == MicroblockProxy.itemMicro &&
            event.getTarget != null && event.getTarget.typeOfHit == RayTraceResult.Type.BLOCK) {
            GlStateManager.pushMatrix()
            RenderUtils.translateToWorldCoords(event.getPlayer, event.getPartialTicks)
            if (ItemMicroPartRenderer.renderHighlight(event.getPlayer, currentItem, event.getTarget)) {
                event.setCanceled(true)
            }
            GlStateManager.popMatrix()
        }
    }

    @SubscribeEvent
    def registerRecipes(event: RegistryEvent.Register[IRecipe]) {
        MicroblockProxy.registerRecipes(event.getRegistry)
    }
}
