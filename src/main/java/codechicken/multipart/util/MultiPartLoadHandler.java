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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by covers1624 on 13/5/20.
 */
public class MultiPartLoadHandler {

    private static final Logger logger = LogManager.getLogger();
    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    public static void init() {
        LOCK.lock();
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
        public CompoundTag tag;

        public TileNBTContainer(BlockPos pos, BlockState state) {
            super(CBMultipartModContent.tileMultipartType, pos, state);
        }

        //Handle initial desc sync
        @Override
        public void handleUpdateTag(CompoundTag tag) {
            if (!tag.contains("data")) {
                logger.warn("Received update tag without 'data' field. Ignoring..");
                return;
            }

            byte[] data = tag.getByteArray("data");
            TileMultiPart.handleDescPacket(getLevel(), getBlockPos(), new MCDataByteBuf(Unpooled.wrappedBuffer(data)));
        }

        @Override
        public void load(CompoundTag compound) {
            super.load(compound);
            if (compound != null) {
                tag = compound.copy();
            }
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
