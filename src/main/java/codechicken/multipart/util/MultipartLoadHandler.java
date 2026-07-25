package codechicken.multipart.util;

import codechicken.lib.util.CCCodecs;
import codechicken.multipart.api.TickableTile;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.init.CBMultipartModContent;
import codechicken.multipart.network.MultiPartNetwork;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Created by covers1624 on 13/5/20.
 */
public class MultipartLoadHandler {

    private static final Logger logger = LogManager.getLogger();
    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    public static void init() {
        LOCK.lock();

        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, MultipartLoadHandler::onChunkLoad);
    }

    // TODO move this to a Mixin.
    // BlockEntity.handleUpdateTag is fired before the chunk is fully added to the world.
    // There is no way in handleUpdateTag to get access to the Chunk the tile is being built into.
    // We can potentially use a Mixin to wrap the calls and handle it.
    private static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide() && event.getChunk() instanceof LevelChunk chunk) {
            for (BlockEntity be : List.copyOf(chunk.getBlockEntities().values())) {
                if (be instanceof TileNBTContainer tile && tile.clientData != null) {
                    Level level = tile.getLevel();
                    TileMultipart.handleDescPacket(level, tile.getBlockPos(), tile.clientData);
                }
            }
        }
    }

    //This is a fallback in the event that our Mixin does not get hit.
    public static class TileNBTContainer extends BlockEntity implements TickableTile {

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

        private @Nullable RegistryFriendlyByteBuf clientData;

        @Nullable
        public Optional<TileMultipart> tile;

        public TileNBTContainer(BlockPos pos, BlockState state) {
            super(CBMultipartModContent.MULTIPART_TILE_TYPE.get(), pos, state);
        }

        //Handle initial desc sync
        @Override
        public void handleUpdateTag(ValueInput input) {
            clientData = input.read("data", CCCodecs.embeddedPacket(level.registryAccess())).orElseThrow();
        }

        @Override
        protected void loadAdditional(ValueInput input) {
            super.loadAdditional(input);
            tile = Optional.ofNullable(TileMultipart.fromNBT(input, getBlockPos()));
        }

        @Override
        public void saveAdditional(ValueOutput output) {
            super.saveAdditional(output);
            if (tile != null) {
                tile.ifPresent(t -> t.saveCustomOnly(output));
            }
        }

        @Override
        public void tick() {
            if (level == null || level.isClientSide()) {
                return;
            }

            if (!failed && !loaded) {
                if (tile != null) {
                    if (tile.isPresent()) {
                        var newTile = tile.get();
                        newTile.clearRemoved();
                        level.setBlockEntity(newTile);
                        newTile.notifyTileChange();
                        newTile.notifyShapeChange();
                        MultiPartNetwork.sendDescUpdate(newTile);
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
