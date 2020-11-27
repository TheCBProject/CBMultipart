package codechicken.multipart.util;

import codechicken.lib.data.MCDataByteBuf;
import codechicken.lib.util.CrashLock;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.init.ModContent;
import codechicken.multipart.network.MultipartSPH;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by covers1624 on 13/5/20.
 */
public class MultiPartLoadHandler {

    private static final Logger logger = LogManager.getLogger();
    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    public static void init() {
        LOCK.lock();
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, MultiPartLoadHandler::onChunkDataLoad);
    }

    private static void onChunkDataLoad(ChunkDataEvent.Load event) {
        if (event.getStatus() == ChunkStatus.Type.LEVELCHUNK) {
            Chunk chunk = (Chunk) event.getChunk();
            processTiles(chunk.getTileEntityMap());
        } else {
            ChunkPrimer primer = (ChunkPrimer) event.getChunk();
            processTiles(primer.getTileEntities());
        }
    }

    private static void processTiles(Map<BlockPos, TileEntity> tiles) {
        Iterator<Map.Entry<BlockPos, TileEntity>> iterator = tiles.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, TileEntity> entry = iterator.next();
            if (entry.getValue() instanceof TileNBTContainer) {
                TileEntity newTile = TileMultipart.createFromNBT(((TileNBTContainer) entry.getValue()).tag);
                if (newTile != null) {
                    newTile.validate();
                    entry.setValue(newTile);
                } else {
                    iterator.remove();
                }
            }
        }
    }

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
            super(ModContent.tileMultipartType);
        }

        //Handle initial desc sync
        @Override
        public void handleUpdateTag(CompoundNBT tag) {
            byte[] data = tag.getByteArray("data");
            TileMultipart.handleDescPacket(world, pos, new MCDataByteBuf(Unpooled.wrappedBuffer(data)));
        }

        @Override
        public void read(CompoundNBT compound) {
            super.read(compound);
            if (compound != null) {
                tag = compound.copy();
            }
        }

        @Override
        public CompoundNBT write(CompoundNBT compound) {
            if (tag != null) {
                compound.merge(tag);
            }
            return super.write(compound);
        }

        @Override
        public void tick() {
            if (!failed && !loaded) {
                if (tag != null) {
                    TileMultipart newTile = TileMultipart.createFromNBT(tag);
                    if (newTile != null) {
                        newTile.validate();
                        world.setTileEntity(pos, newTile);
                        newTile.notifyTileChange();
                        MultipartSPH.sendDescUpdate(newTile);
                    } else {
                        world.removeBlock(pos, false);
                    }
                    loaded = true;
                } else {
                    ticks += 1;
                    if ((ticks % 600) == 0) {
                        failed = true;
                        logger.warn("TileNBTContainer at '{}' still exists after {} ticks! Deleting..", pos, ticks);
                        world.removeBlock(pos, false);
                    }
                }
            }
        }
    }
}
