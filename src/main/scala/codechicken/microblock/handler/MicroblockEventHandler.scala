package codechicken.microblock.handler

import codechicken.lib.render.RenderUtils
import codechicken.microblock.{ItemMicroBlockRenderer, MicroMaterialRegistry}
import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.client.event.DrawHighlightEvent.HighlightBlock
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.eventbus.api.{EventPriority, SubscribeEvent}

object MicroblockEventHandler {
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    def postTextureStitch(event: TextureStitchEvent.Post) {
        MicroMaterialRegistry.markIconReload()
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    @OnlyIn(Dist.CLIENT)
    def drawBlockHighlight(event: HighlightBlock) {

        event.getInfo.getRenderViewEntity match {
            case player: PlayerEntity =>
                val currentItem = player.getHeldItemMainhand

                if (!currentItem.isEmpty && currentItem.getItem == MicroblockModContent.itemMicroBlock) {
                    val mStack = event.getMatrix
                    val info = event.getInfo
                    mStack.push()
                    mStack.translate(-info.getProjectedView.x, -info.getProjectedView.y, -info.getProjectedView.z)
                    if (ItemMicroBlockRenderer.renderHighlight(player, Hand.MAIN_HAND ,currentItem, event.getTarget, mStack, event.getBuffers, event.getPartialTicks)) {
                        event.setCanceled(true)
                    }
                    mStack.pop()
                }
            case _ =>
        }
    }

    //    @SubscribeEvent
    //    def registerRecipes(event: RegistryEvent.Register[IRecipe]) {
    //        MicroblockProxy.registerRecipes(event.getRegistry)
    //    }
}
