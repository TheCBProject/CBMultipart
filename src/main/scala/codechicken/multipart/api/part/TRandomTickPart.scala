package codechicken.multipart.api.part

import codechicken.multipart.util.TickScheduler
import net.minecraft.world.chunk.Chunk

/**
 * Interface for parts with random update ticks. Used in conjuction with TickScheduler
 */
trait TRandomTickPart extends TMultiPart {
    /**
     * Called on random update. Random ticks are between 800 and 1600 ticks from their last scheduled/random tick
     */
    def randomTick(): Unit

    override def onChunkLoad(chunk: Chunk) {
        TickScheduler.loadRandomTick(this, chunk)
    }

    /**
     * If implementing interface in java, be sure to implement this method yourself
     */
    override def onWorldJoin() {
        TickScheduler.loadRandomTick(this)
    }
}
