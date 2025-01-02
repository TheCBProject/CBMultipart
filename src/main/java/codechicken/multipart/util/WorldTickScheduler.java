package codechicken.multipart.util;

import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.RandomTickPart;
import codechicken.multipart.block.TileMultipart;
import net.covers1624.quack.collection.FastStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static codechicken.multipart.CBMultipart.MOD_ID;

/**
 * Internal Impl for TickScheduler.
 * Created by covers1624 on 12/5/20.
 */
class WorldTickScheduler extends SavedData {

    public static WorldTickScheduler getInstance(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(
                        () -> new WorldTickScheduler(level),
                        (t, r) -> new WorldTickScheduler(level, t)
                ),
                MOD_ID + "_scheduled_ticks"
        );
    }

    public static ChunkScheduler getInstance(LevelChunk chunk) {
        return getInstance((ServerLevel) chunk.getLevel())
                .getChunkScheduler(chunk);
    }

    private final ServerLevel world;

    private final Map<ChunkPos, ChunkScheduler> chunks = new HashMap<>();
    private final List<ChunkScheduler> ticking = new LinkedList<>();

    private final List<ChunkScheduler> tickingPending = new LinkedList<>();
    private boolean isTicking;

    WorldTickScheduler(ServerLevel world) {
        this.world = world;
    }

    WorldTickScheduler(ServerLevel world, CompoundTag tag) {
        this.world = world;
        load(tag);
    }

    private void load(CompoundTag tag) {
        ListTag chunksList = tag.getList("Chunks", Tag.TAG_COMPOUND);
        for (int i = 0; i < chunksList.size(); i++) {
            CompoundTag chunkTag = chunksList.getCompound(i);
            ChunkPos pos = new ChunkPos(chunkTag.getInt("ChunkX"), chunkTag.getInt("ChunkZ"));
            ChunkScheduler chunkScheduler = new ChunkScheduler(this, pos);
            chunkScheduler.load(chunkTag);
            chunks.put(pos, chunkScheduler);
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag chunksList = new ListTag();
        for (ChunkScheduler chunk : chunks.values()) {
            CompoundTag chunkTag = chunk.save(new CompoundTag());
            if (chunkTag == null) continue;

            chunkTag.putInt("ChunkX", chunk.pos.x);
            chunkTag.putInt("ChunkZ", chunk.pos.z);
            chunksList.add(chunkTag);
        }
        tag.put("Chunks", chunksList);

        return tag;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    public ChunkScheduler getChunkScheduler(LevelChunk chunk) {
        return chunks.computeIfAbsent(chunk.getPos(), pos -> {
            ChunkScheduler scheduler = new ChunkScheduler(this, pos);
            if (chunk.loaded) {
                scheduler.onChunkLoad(chunk);
            }
            return scheduler;
        });
    }

    public void onChunkUnload(LevelChunk chunk) {
        ChunkPos pos = chunk.getPos();
        ticking.removeIf(e -> {
            if (!e.pos.equals(pos)) return false;

            e.onChunkUnload();
            return true;
        });
    }

    public void onChunkLoad(LevelChunk chunk) {
        ChunkPos pos = chunk.getPos();
        ChunkScheduler scheduler = chunks.get(pos);
        if (scheduler != null) {
            scheduler.onChunkLoad(chunk);
        }
    }

    public void tick() {
        isTicking = true;
        ticking.removeIf(ChunkScheduler::tick);
        isTicking = false;
        ticking.addAll(tickingPending);
        tickingPending.clear();
    }

    public void startTicking(ChunkScheduler chunkScheduler) {
        if (isTicking) {
            tickingPending.add(chunkScheduler);
        } else {
            ticking.add(chunkScheduler);
        }
    }

    static class ChunkScheduler {

        private final WorldTickScheduler worldScheduler;
        private final ChunkPos pos;
        private @Nullable LevelChunk chunk;

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

        ChunkScheduler(WorldTickScheduler worldScheduler, ChunkPos pos) {
            this.worldScheduler = worldScheduler;
            this.pos = pos;
        }

        private void load(CompoundTag tag) {
            FastStream.of(tag.getList("ticks", 10))
                    .map(e -> ((CompoundTag) e))
                    .map(SavedTickEntry::new)
                    .forEach(savedTicks::add);
        }

        private @Nullable CompoundTag save(CompoundTag tag) {
            if (scheduledTicks.isEmpty() && savedTicks.isEmpty()) return null;

            ListTag tickList = new ListTag();
            FastStream.of(scheduledTicks)
                    .map(PartTickEntry::write)
                    .filter(Objects::nonNull)
                    .forEach(tickList::add);
            // Just incase weird things happen.
            savedTicks.forEach(e -> tickList.add(e.write()));
            tag.put("ticks", tickList);

            return tag;
        }

        public void addScheduledTick(MultiPart part, int time) {
            PartTickEntry entry = new PartTickEntry(part, worldScheduler.world.getGameTime() + time, false);
            if (ticking) {
                pendingScheduled.add(entry);
            } else {
                scheduledTicks.add(entry);
                onAdd();
            }
        }

        public void loadRandomTick(MultiPart part) {
            addRandomTick(part, worldScheduler.world.getGameTime() + nextRandomTick());
        }

        public void addRandomTick(MultiPart part, long time) {
            PartTickEntry entry = new PartTickEntry(part, time, true);
            if (ticking) {
                pendingRandom.add(entry);
            } else {
                randomTicks.add(entry);
                onAdd();
            }
        }

        private void onAdd() {
            if (scheduledTicks.isEmpty() && randomTicks.isEmpty()) return;

            worldScheduler.startTicking(this);
        }

        private void onChunkUnload() {
            chunk = null;
        }

        //TODO, I can see future problems clearing this, if TileNBTContainer loads the part X ticks later.
        //Perhaps this should also wait X ticks after chunk load for those cases.
        //But TileNBTContainer _should_ only exist for parts placed runtime by things not aware of the API.
        private void onChunkLoad(LevelChunk chunk) {
            if (this.chunk != null) throw new RuntimeException("Chunk already loaded?");

            this.chunk = chunk;
            for (SavedTickEntry savedTick : savedTicks) {
                //Use map to avoid loading locks.
                BlockEntity tileEntity = chunk.getBlockEntities().get(savedTick.pos);
                if (tileEntity instanceof TileMultipart tile) {
                    scheduledTicks.add(new PartTickEntry(tile.getPartList().get(savedTick.idx), savedTick.time, false));
                }
            }
            savedTicks.clear();
            onAdd();
        }

        private boolean tick() {
            if (chunk == null) return true;

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
            long time = worldScheduler.world.getGameTime();
            list.removeIf(entry -> {
                if (entry.time > time) return false;

                if (entry.part.hasTile()) {
                    if (entry.random) {
                        if (entry.part instanceof RandomTickPart) {
                            ((RandomTickPart) entry.part).randomTick();
                        }
                        addRandomTick(entry.part, time + nextRandomTick());
                    } else {
                        entry.part.scheduledTick();
                    }
                }
                return true;
            });
        }

        private int nextRandomTick() {
            return worldScheduler.world.getRandom().nextInt(800) + 800;
        }
    }

    private static class PartTickEntry {

        public final MultiPart part;
        public final long time;
        public final boolean random;

        private PartTickEntry(MultiPart part, long time, boolean random) {
            this.part = part;
            this.time = time;
            this.random = random;
        }

        @Nullable
        public CompoundTag write() {
            if (!part.hasTile()) return null;

            CompoundTag tag = new CompoundTag();
            tag.put("pos", NbtUtils.writeBlockPos(part.pos()));
            tag.putInt("idx", part.tile().getPartList().indexOf(part));
            tag.putLong("time", time);
            return tag;
        }
    }

    private static class SavedTickEntry {

        public final BlockPos pos;
        public final int idx;
        public final long time;

        public SavedTickEntry(CompoundTag tag) {
            pos = readBlockPos(tag.getCompound("pos"));
            idx = tag.getInt("idx");
            time = tag.getLong("time");
        }

        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.put("pos", writeBlockPos(pos));
            tag.putInt("idx", idx);
            tag.putLong("time", time);
            return tag;
        }

        private static BlockPos readBlockPos(CompoundTag tag) {
            return new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
        }

        private static CompoundTag writeBlockPos(BlockPos tag) {
            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putInt("X", tag.getX());
            compoundtag.putInt("Y", tag.getY());
            compoundtag.putInt("Z", tag.getZ());
            return compoundtag;
        }
    }
}
