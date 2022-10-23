package codechicken.multipart.util;

import codechicken.lib.data.MCDataByteBuf;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.init.CBMultipartModContent;
import codechicken.multipart.network.MultiPartSPH;
import io.netty.buffer.Unpooled;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by covers1624 on 13/5/20.
 */
public class MultiPartLoadHandler {

    private static final Logger logger = LogManager.getLogger();
    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    public static void init() {
        LOCK.lock();

        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, MultiPartLoadHandler::onChunkLoad);
    }

    // TODO move this to a Mixin.
    // Vanilla fires BlockEntity.handleUpdateTag before the LevelChunk has been added to the world.
    private static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getWorld().isClientSide() && event.getChunk() instanceof LevelChunk chunk) {
            for (BlockEntity be : List.copyOf(chunk.getBlockEntities().values())) {
                if (be instanceof TileNBTContainer tile && tile.updateTag != null) {
                    byte[] data = tile.updateTag.getByteArray("data");
                    TileMultiPart.handleDescPacket(tile.getLevel(), tile.getBlockPos(), new MCDataByteBuf(Unpooled.wrappedBuffer(data)));
                }
            }
        }
    }

    //This is a fallback in the event that our Mixin does not get hit.
    public static class TileNBTContainer extends BlockEntity {

        //Store the number of ticks this tile has existed for.
        //We use this to remove the tile from the ticking list
        //after it has existed for too long.
        private int ticks;
        //If the tile has taken too long to load.
        private boolean failed;
        //If the tile has successfully loaded.
        //Here just in case something weird happens,
        //we don't load it multiple times.
        private boolean loaded;
        //The NBT of the tile.
        //We save this back out in case something breaks.
        @Nullable
        public CompoundTag tag;

        @Nullable
        public CompoundTag updateTag;

        public TileNBTContainer(BlockPos pos, BlockState state) {
            super(CBMultipartModContent.MULTIPART_TILE_TYPE.get(), pos, state);
        }

        //Handle initial desc sync
        @Override
        public void handleUpdateTag(CompoundTag tag) {
            if (!tag.contains("data")) {
                logger.warn("Received update tag without 'data' field. Ignoring..");
                return;
            }
            updateTag = tag;
        }

        @Override
        public void load(CompoundTag compound) {
            super.load(compound);
            tag = compound.copy();
        }

        @Override
        public void saveAdditional(CompoundTag compound) {
            super.saveAdditional(compound);
            if (tag != null) {
                compound.merge(tag);
            }
        }

        public void tick() {
            if (level == null || level.isClientSide) {
                return;
            }

            if (!failed && !loaded) {
                if (tag != null) {
                    TileMultiPart newTile = TileMultiPart.fromNBT(tag, getBlockPos());
                    if (newTile != null) {
                        newTile.clearRemoved();
                        level.setBlockEntity(newTile);
                        newTile.notifyTileChange();
                        MultiPartSPH.sendDescUpdate(newTile);
                    } else {
                        level.removeBlock(getBlockPos(), false);
                    }
                    loaded = true;
                } else {
                    ticks += 1;
                    if ((ticks % 600) == 0) {
                        failed = true;
                        logger.warn("TileNBTContainer at '{}' still exists after {} ticks! Deleting..", getBlockPos(), ticks);
                        level.removeBlock(getBlockPos(), false);
                    }
                }
            }
        }
    }
}
