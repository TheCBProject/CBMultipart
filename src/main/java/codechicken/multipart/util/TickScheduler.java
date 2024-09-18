package codechicken.multipart.util;

import codechicken.multipart.CBMultipart;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.RandomTickPart;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

/**
 * Used to Schedule ticks for {@link MultiPart} instances.
 * You probably want {@link MultiPart#scheduleTick(int)} and {@link RandomTickPart}.
 * <p>
 * If 2 parts are scheduled on the same tick, there is no guarantee on which part will
 * receive their update first. If a tick is scheduled and the owning chunk unloaded,
 * and subsequently the scheduled time passes, the tick will be fired immediately on
 * the next tick after the chunk loads.
 * Created by covers1624 on 12/5/20.
 */
public class TickScheduler {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    public static void init() {
        LOCK.lock();
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, TickScheduler::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, TickScheduler::onChunkUnload);
        NeoForge.EVENT_BUS.addListener(TickScheduler::onWorldTick);
    }

    private static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        WorldTickScheduler scheduler = WorldTickScheduler.getInstance(level);
        scheduler.onChunkLoad(chunk);
    }

    private static void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        WorldTickScheduler scheduler = WorldTickScheduler.getInstance(level);
        scheduler.onChunkUnload(chunk);
    }

    private static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (!(event.level instanceof ServerLevel level) || event.phase != TickEvent.Phase.END) {
            return;
        }
        WorldTickScheduler worldScheduler = WorldTickScheduler.getInstance(level);
        worldScheduler.tick();
    }

    /**
     * Schedule a tick for the given part, relative to game time.
     * It should be noted, that if the chunk is un-loaded whilst the tick
     * is scheduled, it will be fired immediately on chunk load.
     *
     * @param part  The part to receive a tick.
     * @param ticks The number of ticks in the future.
     */
    public static void scheduleTick(MultiPart part, int ticks) {
        if (part.level() instanceof ServerLevel level) {
            ChunkAccess chunk = level.getChunk(part.pos());
            if (chunk instanceof LevelChunk lc) {
                WorldTickScheduler.ChunkScheduler chunkScheduler = WorldTickScheduler.getInstance(lc);
                chunkScheduler.addScheduledTick(part, ticks);
            }
        }
    }

    /**
     * Loads random ticks for the given part.
     * Called from {@link RandomTickPart#onWorldJoin()}.
     *
     * @param part The part.
     */
    public static void loadRandomTick(RandomTickPart part) {
        if (part.level() instanceof ServerLevel level) {
            ChunkAccess chunk = level.getChunk(part.pos());
            if (chunk instanceof LevelChunk) {
                loadRandomTick(part, (LevelChunk) chunk);
            }
        }
    }

    /**
     * Loads random ticks for the given part.
     * Called from {@link RandomTickPart#onWorldJoin()}.
     *
     * @param part  The part.
     * @param chunk The chunk.
     */
    public static void loadRandomTick(RandomTickPart part, LevelChunk chunk) {
        if (chunk.getLevel() instanceof ServerLevel) {
            WorldTickScheduler.ChunkScheduler chunkScheduler = WorldTickScheduler.getInstance(chunk);
            chunkScheduler.loadRandomTick(part);
        }
    }

}
