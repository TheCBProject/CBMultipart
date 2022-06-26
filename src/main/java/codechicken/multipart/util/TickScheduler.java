package codechicken.multipart.util;

import codechicken.lib.capability.SimpleCapProvider;
import codechicken.multipart.CBMultipart;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.TRandomTickPart;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

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

    private static final Capability<WorldTickScheduler> WORLD_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() { });
    private static final Capability<WorldTickScheduler.ChunkScheduler> CHUNK_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() { });

    private static final ResourceLocation WORLD_KEY = new ResourceLocation(CBMultipart.MOD_ID, "world_scheduled_ticks");
    private static final ResourceLocation CHUNK_KEY = new ResourceLocation(CBMultipart.MOD_ID, "chunk_scheduled_ticks");

    public static void init() {
        LOCK.lock();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(TickScheduler::registerCapabilities);
        MinecraftForge.EVENT_BUS.addGenericListener(Level.class, TickScheduler::attachLevelCapabilities);
        MinecraftForge.EVENT_BUS.addGenericListener(LevelChunk.class, TickScheduler::attachChunkCapabilities);
        MinecraftForge.EVENT_BUS.addListener(TickScheduler::onChunkLoad);
        MinecraftForge.EVENT_BUS.addListener(TickScheduler::onChunkUnload);
        MinecraftForge.EVENT_BUS.addListener(TickScheduler::onWorldTick);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(WorldTickScheduler.class);
        event.register(WorldTickScheduler.ChunkScheduler.class);
    }

    private static void attachLevelCapabilities(AttachCapabilitiesEvent<Level> event) {
        if (!(event.getObject() instanceof ServerLevel world)) {
            return;
        }
        WorldTickScheduler scheduler = new WorldTickScheduler(world);
        event.addCapability(WORLD_KEY, new SimpleCapProvider<>(WORLD_CAPABILITY, scheduler));
    }

    private static void attachChunkCapabilities(AttachCapabilitiesEvent<LevelChunk> event) {
        if (!(event.getObject().getLevel() instanceof ServerLevel level)) {
            return;
        }
        WorldTickScheduler worldScheduler = WorldTickScheduler.getInstance(level);
        WorldTickScheduler.ChunkScheduler scheduler = new WorldTickScheduler.ChunkScheduler(worldScheduler, event.getObject());
        LazyOptional<WorldTickScheduler.ChunkScheduler> opt = LazyOptional.of(() -> scheduler);
        event.addCapability(CHUNK_KEY, new ICapabilitySerializable<>() {

            @Override
            public Tag serializeNBT() {
                return WorldTickScheduler.CHUNK_STORAGE.writeNBT(scheduler);
            }

            @Override
            public void deserializeNBT(Tag nbt) {
                WorldTickScheduler.CHUNK_STORAGE.readNBT(scheduler, nbt);
            }

            @NotNull
            @Override
            public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == CHUNK_CAPABILITY) {
                    return unsafeCast(opt);
                }
                return LazyOptional.empty();
            }
        });
    }

    private static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getWorld() instanceof ServerLevel) || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        WorldTickScheduler.ChunkScheduler chunkScheduler = WorldTickScheduler.getInstance(chunk);
        chunkScheduler.onChunkLoad();
    }

    private static void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getWorld() instanceof ServerLevel level)) {
            return;
        }
        WorldTickScheduler worldScheduler = WorldTickScheduler.getInstance(level);
        worldScheduler.onChunkUnload(event.getChunk().getPos());
    }

    private static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (!(event.world instanceof ServerLevel level) || event.phase != TickEvent.Phase.END) {
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
    public static void scheduleTick(TMultiPart part, int ticks) {
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
     * Called from {@link TRandomTickPart#onWorldJoin()}.
     *
     * @param part The part.
     */
    public static void loadRandomTick(TRandomTickPart part) {
        if (part.level() instanceof ServerLevel level) {
            ChunkAccess chunk = level.getChunk(part.pos());
            if (chunk instanceof LevelChunk) {
                loadRandomTick(part, (LevelChunk) chunk);
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
    public static void loadRandomTick(TRandomTickPart part, LevelChunk chunk) {

        WorldTickScheduler.ChunkScheduler chunkScheduler = WorldTickScheduler.getInstance(chunk);
        chunkScheduler.loadRandomTick(part);
    }

}
