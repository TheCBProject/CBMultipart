package codechicken.multipart

import codechicken.multipart.handler.MultipartProxy
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

import scala.collection.mutable.{Map => MMap}

/**
 * In order to maintain tight client/server synchronisation without bandwidth overhead, all data written must be read.
 * Sometimes the client tile is removed from the chunk before the packet arrives. This class maintains a reference to all multipart tiles.
 * The original design placed tiles that were invalidated into a map that was cleared upon arrival of the update packet.
 * Due to other factors that have eluded identification, references to all tiles held until they are replaced or the world is reloaded.
 */
object TileCache {

    case class FlaggedTile(t: TileMultipart, removed: Boolean)

    val map = MMap[BlockPos, FlaggedTile]()

    def add(t: TileMultipart) = map.put(t.getPos, FlaggedTile(t, false))

    def remove(t: TileMultipart) = map.put(t.getPos, FlaggedTile(t, true))

    def apply(c: BlockPos) = map.get(c)

    def clear() = map.clear()

    def findTile(world: World, c: BlockPos) = BlockMultipart.getTile(world, c) match {
        case null => apply(c) match {
            case Some(FlaggedTile(t, rem)) =>
                if (!rem) {
                    MultipartProxy.logger.warn("Client multipart @" + c + " vanished from world but was recovered. If possible causes can be identified, please report to the github issue tracker.")
                }
                t
            case None =>
                throw new RuntimeException("DC: Client multipart @" + c + " not found")
            case _ =>
                null
        }
        case t => t
    }
}
