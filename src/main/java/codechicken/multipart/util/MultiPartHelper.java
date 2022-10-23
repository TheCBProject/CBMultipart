package codechicken.multipart.util;

import codechicken.lib.packet.PacketCustom;
import codechicken.mixin.api.MixinFactory;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.init.CBMultipartModContent;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.network.MultiPartSPH;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Mostly internal methods.
 * Some things here are user facing, refer to code regions.
 * Methods within the INTERNAL region are subject to change without notice.
 * <p>
 * Created by covers1624 on 4/12/20.
 */
public class MultiPartHelper {

    /**
     * Overload of {@link #getOrConvertTile2} but does not return the converted flag.
     * Useful for checking if a part can be added to the current block space.
     *
     * @param world The world.
     * @param pos   The position.
     * @return The tile.
     */
    @Nullable
    public static TileMultiPart getOrConvertTile(Level world, BlockPos pos) {
        return getOrConvertTile2(world, pos).getLeft();
    }

    /**
     * Queries the tile in world at the provided position and performs conversion if possible.
     * Does NOT modify the world.
     *
     * @param world The world.
     * @param pos   The position.
     * @return A Pair result of, the tile if it exists null otherwise, and a boolean flag if the tile
     * exists as a result of conversion.
     */
    public static Pair<@Nullable TileMultiPart, Boolean> getOrConvertTile2(Level world, BlockPos pos) {
        BlockEntity t = world.getBlockEntity(pos);
        if (t instanceof TileMultiPart) {
            return Pair.of((TileMultiPart) t, false);
        }
        Collection<TMultiPart> parts = MultiPartRegistries.convertBlock(world, pos, world.getBlockState(pos));
        if (!parts.isEmpty()) {
            TileMultiPart tile = MultiPartGenerator.INSTANCE.generateCompositeTile(null, pos, parts, world.isClientSide);
            tile.setLevel(world);
            parts.forEach(tile::addPart_do);
            return Pair.of(tile, true);
        }
        return Pair.of(null, false);
    }

    //region INTERNAL DO NOT USE

    /**
     * INTERNAL METHOD
     * Checks if there are redundant traits on the tile and strips them.
     */
    public static TileMultiPart partRemoved(TileMultiPart tile) {
        TileMultiPart newTile = MultiPartGenerator.INSTANCE.generateCompositeTile(tile, tile.getBlockPos(), tile.getPartList(), tile.getLevel().isClientSide);
        if (tile != newTile) {
            tile.setValid(false);
            silentAddTile(tile.getLevel(), tile.getBlockPos(), newTile);
            newTile.from(tile);
            newTile.notifyTileChange();
        }
        return newTile;
    }

    /**
     * INTERNAL METHOD
     * Performs the necessary operations to add a part to a tile.
     * Checks if any new traits need to be applied to the tile instance, and replaces if so.
     */
    public static TileMultiPart addPart(Level world, BlockPos pos, TMultiPart part) {
        Pair<TileMultiPart, Boolean> pair = getOrConvertTile2(world, pos);
        TileMultiPart tile = pair.getLeft();
        boolean converted = pair.getRight();
        ImmutableSet<MixinFactory.TraitKey> traits = MultiPartGenerator.INSTANCE.getTraits(part, world.isClientSide);

        TileMultiPart newTile = tile;
        if (newTile != null) {
            //If we just converted an in-world block to a TileMultipart
            if (converted) {//TODO, dont use head here? assume all parts are capable of receiving these callbacks
                //Callback to the part, so it can do stuff to inworld tile before it gets nuked.
                TMultiPart head = newTile.getPartList().get(0);
                head.invalidateConvertedTile();
                world.setBlock(pos, CBMultipartModContent.MULTIPART_BLOCK.get().defaultBlockState(), 0);
                silentAddTile(world, pos, newTile);
                PacketCustom.sendToChunk(new ClientboundBlockUpdatePacket(world, pos), world, pos);
                head.onConverted();
                MultiPartSPH.sendAddPart(newTile, head); //TODO, what if a converter converts the tile to more than one part?
            }

            //Get the traits that exist on this TileMultipart instance.
            ImmutableSet<MixinFactory.TraitKey> tileTraits = MultiPartGenerator.INSTANCE.getTraitsForClass(tile.getClass());
            //If the new part has traits the tile doesnt already have.
            if (!tileTraits.containsAll(traits)) {
                //concat traits and generate new tile.
                ImmutableSet<MixinFactory.TraitKey> newTraits = ImmutableSet.<MixinFactory.TraitKey>builder()//
                        .addAll(tileTraits).addAll(traits).build();
                newTile = MultiPartGenerator.INSTANCE.construct(newTraits).newInstance(pos, CBMultipartModContent.MULTIPART_BLOCK.get().defaultBlockState());
                newTile.setValid(false);
                silentAddTile(world, pos, newTile);
                newTile.from(tile);
            }
        } else {
            //Nothing exists in world, just create a new tile with the required traits.
            world.setBlock(pos, CBMultipartModContent.MULTIPART_BLOCK.get().defaultBlockState(), 0);
            newTile = MultiPartGenerator.INSTANCE.construct(traits).newInstance(pos, CBMultipartModContent.MULTIPART_BLOCK.get().defaultBlockState());
            silentAddTile(world, pos, newTile);
        }
        //actually add the part to the tile.
        newTile.addPart_impl(part);
        return newTile;
    }

    /**
     * INTERNAL METHOD
     * Swaps the tile directly on the chunk.
     */
    public static void silentAddTile(Level world, BlockPos pos, BlockEntity tile) {
        ChunkAccess chunk = world.getChunk(pos);
        chunk.setBlockEntity(tile);
    }
    //endregion
}
