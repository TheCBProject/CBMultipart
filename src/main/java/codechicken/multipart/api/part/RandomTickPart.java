package codechicken.multipart.api.part;

import codechicken.multipart.util.TickScheduler;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Interface for parts with random update ticks.
 * <p>
 * Used in conjunction with {@link TickScheduler}
 */
public interface RandomTickPart extends MultiPart {

    /**
     * Called on random update.
     * <p>
     * Random ticks are between 800 and 1600 ticks from their last scheduled/random tick.
     */
    void randomTick();

    @Override
    default void onChunkLoad(LevelChunk chunk) {
        TickScheduler.loadRandomTick(this, chunk);
    }

    @Override
    default void onWorldJoin() {
        TickScheduler.loadRandomTick(this);
    }
}
