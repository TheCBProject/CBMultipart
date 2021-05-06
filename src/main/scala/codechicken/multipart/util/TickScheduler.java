package codechicken.multipart.util;

import codechicken.lib.capability.NullStorage;
import codechicken.lib.capability.SimpleCapProvider;
import codechicken.lib.capability.SimpleCapProviderSerializable;
import codechicken.lib.util.CrashLock;
import codechicken.multipart.CBMultipart;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.TRandomTickPart;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimerWrapper;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkEvent;

/**
 * Used to Schedule ticks for {@link TMultiPart} instances.
 * You probably want {@link TMultiPart#scheduleTick(int)} and {@link TRandomTickPart}.
 * <p>
 * If 2 parts are scheduled on the same tick, there is no guarantee on which part will
 * receive their update first. If a tick is scheduled and the owning chunk unloaded,
 * and subsequently the scheduled time passes, the tick will be fired immediately on
 * the next tick after the chunk loads.
 * Created by covers1624 on 12/5/20.
 */
public class TickScheduler {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    //TODO, Forge has issues with these being final even though other Capability instances are final.
    @CapabilityInject (WorldTickScheduler.class)
    private static Capability<WorldTickScheduler> WORLD_CAPABILITY = null;
    @CapabilityInject (WorldTickScheduler.ChunkScheduler.class)
    private static Capability<WorldTickScheduler.ChunkScheduler> CHUNK_CAPABILITY = null;

    private static final ResourceLocation WORLD_KEY = new ResourceLocation(CBMultipart.MOD_ID, "world_scheduled_ticks");
    private static final ResourceLocation CHUNK_KEY = new ResourceLocation(CBMultipart.MOD_ID, "chunk_scheduled_ticks");

    public static void init() {
        LOCK.lock();
        MinecraftForge.EVENT_BUS.addGenericListener(World.class, TickScheduler::attachWorldCapabilities);
        MinecraftForge.EVENT_BUS.addGenericListener(Chunk.class, TickScheduler::attachChunkCapabilities);
        MinecraftForge.EVENT_BUS.addListener(TickScheduler::onChunkLoad);
        MinecraftForge.EVENT_BUS.addListener(TickScheduler::onChunkUnload);
        MinecraftForge.EVENT_BUS.addListener(TickScheduler::onWorldTick);
        CapabilityManager.INSTANCE.register(WorldTickScheduler.class, new NullStorage<>(), () -> null);
        CapabilityManager.INSTANCE.register(WorldTickScheduler.ChunkScheduler.class, new WorldTickScheduler.ChunkStorage(), () -> null);
    }

    private static void attachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        if (!(event.getObject() instanceof ServerWorld)) {
            return;
        }
        ServerWorld world = (ServerWorld) event.getObject();
        WorldTickScheduler scheduler = new WorldTickScheduler(world);
        event.addCapability(WORLD_KEY, new SimpleCapProvider<>(WORLD_CAPABILITY, scheduler));
    }

    private static void attachChunkCapabilities(AttachCapabilitiesEvent<Chunk> event) {
        if (!(event.getObject().getLevel() instanceof ServerWorld)) {
            return;
        }
        ServerWorld level = (ServerWorld) event.getObject().getLevel();
        WorldTickScheduler worldScheduler = WorldTickScheduler.getInstance(level);
        WorldTickScheduler.ChunkScheduler scheduler = new WorldTickScheduler.ChunkScheduler(worldScheduler, event.getObject());
        event.addCapability(CHUNK_KEY, new SimpleCapProviderSerializable<>(CHUNK_CAPABILITY, scheduler));
    }

    private static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getWorld() instanceof ServerWorld)) {
            return;
        }
        IChunk iChunk = event.getChunk();
        Chunk chunk;
        if (iChunk instanceof Chunk) {
            chunk = (Chunk) iChunk;
        } else if (iChunk instanceof ChunkPrimerWrapper) {
            //This should never happen due to event locations.
            chunk = ((ChunkPrimerWrapper) iChunk).getWrapped();
        } else {
            //We dont care about proto chunks.
            return;
        }
        WorldTickScheduler.ChunkScheduler chunkScheduler = WorldTickScheduler.getInstance(chunk);
        chunkScheduler.onChunkLoad();
    }

    private static void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getWorld() instanceof ServerWorld)) {
            return;
        }
        WorldTickScheduler worldScheduler = WorldTickScheduler.getInstance((ServerWorld) event.getWorld());
        worldScheduler.onChunkUnload(event.getChunk().getPos());
    }

    private static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (!(event.world instanceof ServerWorld) || event.phase != TickEvent.Phase.END) {
            return;
        }
        WorldTickScheduler worldScheduler = WorldTickScheduler.getInstance((ServerWorld) event.world);
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
    public static void scheduleTick(TMultiPart part, int ticks) {
        if (part.world() instanceof ServerWorld) {
            IChunk chunk = part.world().getChunk(part.pos());
            if (chunk instanceof Chunk) {//Should always be the case unless world gen.
                WorldTickScheduler.ChunkScheduler chunkScheduler = WorldTickScheduler.getInstance((Chunk) chunk);
                chunkScheduler.addScheduledTick(part, ticks);
            }
        }
    }

    /**
     * Loads random ticks for the given part.
     * Called from {@link TRandomTickPart#onWorldJoin()}.
     *
     * @param part The part.
     */
    public static void loadRandomTick(TRandomTickPart part) {
        @SuppressWarnings ("RedundantCast")
        TMultiPart thePart = (TMultiPart) part;
        if (thePart.world() instanceof ServerWorld) {
            IChunk chunk = thePart.world().getChunk(thePart.pos());
            if (chunk instanceof Chunk) {//Should always be the case unless world gen.
                loadRandomTick(part, (Chunk) chunk);
            }
        }
    }

    /**
     * Loads random ticks for the given part.
     * Called from {@link TRandomTickPart#onWorldJoin()}.
     *
     * @param part  The part.
     * @param chunk The chunk.
     */
    public static void loadRandomTick(TRandomTickPart part, Chunk chunk) {
        @SuppressWarnings ("RedundantCast")
        TMultiPart thePart = (TMultiPart) part;

        WorldTickScheduler.ChunkScheduler chunkScheduler = WorldTickScheduler.getInstance(chunk);
        chunkScheduler.loadRandomTick(thePart);
    }

}
