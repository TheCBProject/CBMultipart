package codechicken.multipart.util;

import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.RandomTickPart;
import codechicken.multipart.block.TileMultipart;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.covers1624.quack.collection.FastStream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static codechicken.multipart.CBMultipart.MOD_ID;

/**
 * Internal Impl for TickScheduler.
 * Created by covers1624 on 12/5/20.
 */
class WorldTickScheduler extends SavedData {

    private static final SavedDataType<WorldTickScheduler> TYPE = new SavedDataType<>(
            MOD_ID + "_scheduled_ticks",
            WorldTickScheduler::new,
            ctx -> CompoundTag.CODEC.flatXmap(tag -> {
                var scheduler = new WorldTickScheduler(ctx);
                var reporter = new ProblemReporter.Collector();
                scheduler.load(TagValueInput.create(reporter, ctx.getServer().registryAccess(), tag));
                if (reporter.isEmpty()) return DataResult.success(scheduler);
                return DataResult.error(() -> "Failed to deserialize:" + reporter.getReport());
            }, scheduler -> {
                var reporter = new ProblemReporter.Collector();
                var output = TagValueOutput.createWithContext(reporter, ctx.getServer().registryAccess());
                scheduler.save(output);

                if (reporter.isEmpty()) return DataResult.success(output.buildResult());
                return DataResult.error(() -> "Failed to serialize: " + reporter.getReport());
            })
    );

    public static WorldTickScheduler getInstance(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
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

    private void load(ValueInput input) {
        var chunksList = input.childrenListOrEmpty("Chunks");
        for (var chunkTag : chunksList) {
            var pos = chunkTag.read("Pos", ChunkPos.CODEC).orElseThrow();
            ChunkScheduler chunkScheduler = new ChunkScheduler(this, pos);
            chunkScheduler.load(chunkTag);
            chunks.put(pos, chunkScheduler);
        }
    }

    private void save(ValueOutput output) {
        var chunksList = output.childrenList("Chunks");
        for (ChunkScheduler chunk : chunks.values()) {
            var chunkTag = chunksList.addChild();
            chunk.save(chunkTag);
            if (chunkTag.isEmpty()) {
                chunksList.discardLast();
                continue;
            }
            chunkTag.store("Pos", ChunkPos.CODEC, chunk.pos);
        }
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

        private void load(ValueInput input) {
            input.listOrEmpty("ticks", SavedTickEntry.CODEC)
                    .forEach(savedTicks::add);
        }

        private void save(ValueOutput output) {
            if (scheduledTicks.isEmpty() && savedTicks.isEmpty()) return;

            var tickList = output.list("ticks", SavedTickEntry.CODEC);
            for (var tick : scheduledTicks) {
                var saved = tick.save();
                if (saved != null) {
                    tickList.add(saved);
                }
            }
            // Just incase weird things happen.
            savedTicks.forEach(tickList::add);
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

    private record PartTickEntry(MultiPart part, long time, boolean random) {

        @Nullable
        public SavedTickEntry save() {
            if (!part.hasTile()) return null;

            return new SavedTickEntry(
                    part.pos(),
                    part.tile().getPartList().indexOf(part),
                    time
            );
        }
    }

    private record SavedTickEntry(BlockPos pos, int idx, long time) {

        private static final Codec<SavedTickEntry> CODEC = RecordCodecBuilder.create(b -> b.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(SavedTickEntry::pos),
                Codec.INT.fieldOf("idx").forGetter(SavedTickEntry::idx),
                Codec.LONG.fieldOf("time").forGetter(SavedTickEntry::time)
        ).apply(b, SavedTickEntry::new));
    }
}
