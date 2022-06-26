package codechicken.multipart.util;

import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.TRandomTickPart;
import codechicken.multipart.block.TileMultiPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import java.util.*;

/**
 * Internal Impl for TickScheduler.
 * Created by covers1624 on 12/5/20.
 */
class WorldTickScheduler {

    private static final Capability<WorldTickScheduler> WORLD_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() { });
    private static final Capability<WorldTickScheduler.ChunkScheduler> CHUNK_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() { });

    static final ChunkStorage CHUNK_STORAGE = new ChunkStorage();

    public static WorldTickScheduler getInstance(ServerLevel level) {
        return level.getCapability(WORLD_CAPABILITY, null).orElseThrow(() -> new RuntimeException("Should never happen..."));
    }

    public static ChunkScheduler getInstance(LevelChunk chunk) {
        return chunk.getCapability(CHUNK_CAPABILITY).orElseThrow(() -> new RuntimeException("Should never happen..."));
    }

    private final ServerLevel world;

    private final Map<ChunkPos, ChunkScheduler> tickingChunks = new HashMap<>();

    WorldTickScheduler(ServerLevel world) {
        this.world = world;
    }

    public void onChunkUnload(ChunkPos pos) {
        tickingChunks.remove(pos);
    }

    public void tick() {
        if (!tickingChunks.isEmpty()) {
            tickingChunks.values().removeIf(ChunkScheduler::tick);
        }
    }

    static class ChunkScheduler {

        private final WorldTickScheduler worldScheduler;
        private final LevelChunk chunk;

        //Stores the ticks that were loaded from disk, only _loaded_ once ChunkEvent.Load is called.
        private final List<SavedTickEntry> savedTicks = new ArrayList<>();

        //Stores PartTickEntries for scheduled and random ticks.
        //Use LinkedLists for increased performance of random remove.
        private final List<PartTickEntry> scheduledTicks = new LinkedList<>();
        private final List<PartTickEntry> randomTicks = new LinkedList<>();

        //Lock out changes to the above lists whilst we are processing.
        private boolean ticking = false;
        private final List<PartTickEntry> pendingScheduled = new LinkedList<>();
        private final List<PartTickEntry> pendingRandom = new LinkedList<>();

        ChunkScheduler(WorldTickScheduler worldScheduler, LevelChunk chunk) {
            this.worldScheduler = worldScheduler;
            this.chunk = chunk;
        }

        public void addScheduledTick(TMultiPart part, int time) {
            PartTickEntry entry = new PartTickEntry(part, worldScheduler.world.getGameTime() + time, false);
            if (ticking) {
                pendingScheduled.add(entry);
            } else {
                scheduledTicks.add(entry);
                onAdd();
            }
        }

        public void loadRandomTick(TMultiPart part) {
            addRandomTick(part, worldScheduler.world.getGameTime() + nextRandomTick());
        }

        public void addRandomTick(TMultiPart part, long time) {
            PartTickEntry entry = new PartTickEntry(part, time, true);
            if (ticking) {
                pendingRandom.add(entry);
            } else {
                randomTicks.add(entry);
                onAdd();
            }
        }

        private void onAdd() {
            if (!scheduledTicks.isEmpty() || !randomTicks.isEmpty()) {
                worldScheduler.tickingChunks.put(chunk.getPos(), this);
            }
        }

        //TODO, I can see future problems clearing this, if TileNBTContainer loads the part X ticks later.
        //Perhaps this should also wait X ticks after chunk load for those cases.
        //But TileNBTContainer _should_ only exist for parts placed runtime by things not aware of the API.
        public void onChunkLoad() {
            for (SavedTickEntry savedTick : savedTicks) {
                //Use map to avoid loading locks.
                BlockEntity tileEntity = chunk.getBlockEntities().get(savedTick.pos);
                if (tileEntity instanceof TileMultiPart) {
                    TileMultiPart tile = (TileMultiPart) tileEntity;
                    scheduledTicks.add(new PartTickEntry(tile.getPartList().get(savedTick.idx), savedTick.time, false));
                }
            }
            savedTicks.clear();
            onAdd();
        }

        public boolean tick() {
            ticking = true;
            doTicks(scheduledTicks);
            doTicks(randomTicks);
            ticking = false;
            scheduledTicks.addAll(pendingScheduled);
            randomTicks.addAll(pendingRandom);
            pendingScheduled.clear();
            pendingRandom.clear();
            return scheduledTicks.isEmpty() && randomTicks.isEmpty() || !chunk.loaded;
        }

        private void doTicks(List<PartTickEntry> list) {
            Iterator<PartTickEntry> itr = list.iterator();
            long time = worldScheduler.world.getGameTime();
            while (itr.hasNext()) {
                PartTickEntry entry = itr.next();
                if (entry.time <= time) {
                    if (entry.part.tile() != null) {
                        if (entry.random) {
                            if (entry.part instanceof TRandomTickPart) {
                                ((TRandomTickPart) entry.part).randomTick();
                            }
                            addRandomTick(entry.part, time + nextRandomTick());
                        } else {
                            entry.part.scheduledTick();
                        }
                    }
                    itr.remove();
                }
            }
        }

        private int nextRandomTick() {
            return worldScheduler.world.getRandom().nextInt(800) + 800;
        }
    }

    static class ChunkStorage {

        public Tag writeNBT(ChunkScheduler instance) {
            CompoundTag tag = new CompoundTag();

            ListTag scheduledTicks = new ListTag();
            instance.scheduledTicks.stream()
                    .map(PartTickEntry::write)
                    .filter(Objects::nonNull)
                    .forEach(scheduledTicks::add);
            // Just incase weird things happen.
            instance.savedTicks.forEach(e -> scheduledTicks.add(e.write()));
            tag.put("ticks", scheduledTicks);

            return tag;
        }

        public void readNBT(ChunkScheduler instance, Tag nbt) {
            CompoundTag tag = (CompoundTag) nbt;
            tag.getList("ticks", 10).stream()
                    .map(e -> ((CompoundTag) e))
                    .map(SavedTickEntry::new)
                    .forEach(instance.savedTicks::add);
        }
    }

    private static class PartTickEntry {

        public final TMultiPart part;
        public final long time;
        public final boolean random;

        private PartTickEntry(TMultiPart part, long time, boolean random) {
            this.part = part;
            this.time = time;
            this.random = random;
        }

        public CompoundTag write() {
            if (part.tile() != null) {
                CompoundTag tag = new CompoundTag();
                tag.put("pos", NbtUtils.writeBlockPos(part.pos()));
                tag.putInt("idx", part.tile().getPartList().indexOf(part));
                tag.putLong("time", time);
                return tag;
            }
            return null;
        }
    }

    private static class SavedTickEntry {

        public final BlockPos pos;
        public final int idx;
        public final long time;

        public SavedTickEntry(CompoundTag tag) {
            pos = NbtUtils.readBlockPos(tag.getCompound("pos"));
            idx = tag.getInt("idx");
            time = tag.getLong("time");
        }

        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.put("pos", NbtUtils.writeBlockPos(pos));
            tag.putInt("idx", idx);
            tag.putLong("time", time);
            return tag;
        }

    }
}
