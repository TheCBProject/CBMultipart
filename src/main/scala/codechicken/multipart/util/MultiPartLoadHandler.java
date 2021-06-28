package codechicken.multipart.util;

import codechicken.lib.data.MCDataByteBuf;
import codechicken.lib.util.CrashLock;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.init.CBMultipartModContent;
import codechicken.multipart.network.MultiPartSPH;
import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
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
    public static class TileNBTContainer extends TileEntity implements ITickableTileEntity {

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
        public CompoundNBT tag;

        public TileNBTContainer() {
            super(CBMultipartModContent.tileMultipartType);
        }

        //Handle initial desc sync
        @Override
        public void handleUpdateTag(BlockState state, CompoundNBT tag) {
            byte[] data = tag.getByteArray("data");
            TileMultiPart.handleDescPacket(getLevel(), getBlockPos(), new MCDataByteBuf(Unpooled.wrappedBuffer(data)));
        }

        @Override
        public void load(BlockState state, CompoundNBT compound) {
            super.load(state, compound);
            if (compound != null) {
                tag = compound.copy();
            }
        }

        @Override
        public CompoundNBT save(CompoundNBT compound) {
            if (tag != null) {
                compound.merge(tag);
            }
            return super.save(compound);
        }

        @Override
        public void tick() {
            if (level == null || level.isClientSide) {
                return;
            }

            if (!failed && !loaded) {
                if (tag != null) {
                    TileMultiPart newTile = TileMultiPart.fromNBT(tag);
                    if (newTile != null) {
                        newTile.clearRemoved();
                        level.setBlockEntity(getBlockPos(), newTile);
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
