package codechicken.multipart.handler

import codechicken.multipart.{BlockMultipart, TileCache, TileMultipart}
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.event.world._
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.{EventPriority, SubscribeEvent}
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.JavaConversions._

object MultipartEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    def chunkDataLoad(event:ChunkDataEvent.Load) {
        MultipartSaveLoad.loadTiles(event.getChunk)
    }

    @SubscribeEvent
    def worldUnLoad(event: WorldEvent.Unload) {
        MultipartSPH.onWorldUnload(event.getWorld)
        if (event.getWorld.isRemote) {
            TileCache.clear()
        }
    }

    @SubscribeEvent
    def chunkWatch(event: ChunkWatchEvent.Watch) {
        MultipartSPH.onChunkWatch(event.getPlayer, event.getChunk)
    }

    @SubscribeEvent
    def chunkUnWatch(event: ChunkWatchEvent.UnWatch) {
        MultipartSPH.onChunkUnWatch(event.getPlayer, event.getChunk)
    }

    @SubscribeEvent
    def serverTick(event: TickEvent.ServerTickEvent) {
        if (event.phase == TickEvent.Phase.END) {
            MultipartSPH.onTickEnd(FMLCommonHandler.instance().getMinecraftServerInstance.getPlayerList.getPlayers)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    @SideOnly(Side.CLIENT)
    def drawBlockHighlight(event: DrawBlockHighlightEvent) {
        if (event.getTarget != null && event.getTarget.typeOfHit == RayTraceResult.Type.BLOCK &&
            event.getPlayer.world.getTileEntity(event.getTarget.getBlockPos).isInstanceOf[TileMultipart]) {
            if (BlockMultipart.drawHighlight(event.getPlayer.world, event.getPlayer, event.getTarget, event.getPartialTicks)) {
                event.setCanceled(true)
            }
        }
    }
}
