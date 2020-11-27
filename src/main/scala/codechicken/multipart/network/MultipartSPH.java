package codechicken.multipart.network;

import codechicken.lib.data.MCByteStream;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.util.CrashLock;
import codechicken.lib.util.LazyValuePair;
import codechicken.multipart.handler.PlacementConversionHandler;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.util.ControlKeyModifier;
import com.google.common.annotations.VisibleForTesting;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static codechicken.lib.math.MathHelper.between;
import static codechicken.multipart.network.MultipartNetwork.*;

/**
 * Created by covers1624 on 4/30/20.
 */
public class MultipartSPH implements ICustomPacketHandler.IServerPacketHandler {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");
    private static final Map<UUID, BlockPos> playerLastPositions = new HashMap<>();

    public static void init() {
        LOCK.lock();
        MinecraftForge.EVENT_BUS.addListener(MultipartSPH::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(MultipartSPH::onPlayerLoggedOut);
    }

    @Override
    public void handlePacket(PacketCustom packet, ServerPlayerEntity sender, IServerPlayNetHandler handler) {
        switch (packet.getType()) {
            case S_CONTROL_KEY_MODIFIER: {
                ControlKeyModifier.setIsControlDown(sender, packet.readBoolean());
                break;
            }
            case S_MULTIPART_PLACEMENT: {
                PlacementConversionHandler.place(sender, packet.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND, sender.world);
                break;
            }
        }
    }

    //region Send C_TILE_DESC
    public static void sendDescUpdate(TileMultipart tile) {
        ServerWorld world = (ServerWorld) tile.getWorld();
        Stream<ServerPlayerEntity> players = world.getChunkProvider().chunkManager.getTrackingPlayers(new ChunkPos(tile.getPos()), false);
        sendDescUpdates(players, Collections.singleton(tile));
    }

    public static void sendDescUpdates(Stream<ServerPlayerEntity> players, Collection<TileEntity> tiles) {
        if (tiles.isEmpty()) {
            return;
        }
        List<Pair<TileMultipart, MCByteStream>> data = tiles.stream()//
                .filter(e -> e instanceof TileMultipart)//
                .map(e -> (TileMultipart) e)//
                .map(tile -> LazyValuePair.of(tile, t -> {
                    MCByteStream stream = new MCByteStream();
                    t.writeDesc(stream);
                    return stream;
                }))//
                .collect(Collectors.toList());
        if (data.isEmpty()) {
            return;
        }
        players.forEach(player -> sendDescUpdateTo(player, data));
    }

    private static void sendDescUpdateTo(ServerPlayerEntity player, List<Pair<TileMultipart, MCByteStream>> data) {
        PacketCustom packet = new PacketCustom(NET_CHANNEL, C_TILE_DESC);
        packet.writeVarInt(data.size());
        for (Pair<TileMultipart, MCByteStream> entry : data) {
            writeHeaderFor(player, packet, 0, entry.getLeft().getPos());
            packet.append(entry.getRight().getBytes());
        }
        packet.sendToPlayer(player);
    }
    //endregion

    //region Send C_ADD_PART
    public static void sendAddPart(TileMultipart tile, TMultiPart part) {
        ServerWorld world = (ServerWorld) tile.getWorld();
        MCByteStream stream = new MCByteStream();
        MultiPartRegistries.writePart(stream, part);
        world.getChunkProvider().chunkManager.getTrackingPlayers(new ChunkPos(tile.getPos()), false)//
                .forEach(player -> {
                    PacketCustom packet = new PacketCustom(NET_CHANNEL, C_ADD_PART);
                    writeHeaderFor(player, packet, 0, tile.getPos());
                    packet.append(stream.getBytes());
                    packet.sendToPlayer(player);
                });
    }
    //endregion

    //region Send C_REM_PART
    public static void sendRemPart(TileMultipart tile, int partIdx) {
        ServerWorld world = (ServerWorld) tile.getWorld();
        world.getChunkProvider().chunkManager.getTrackingPlayers(new ChunkPos(tile.getPos()), false)//
                .forEach(player -> {
                    PacketCustom packet = new PacketCustom(NET_CHANNEL, C_REM_PART);
                    writeHeaderFor(player, packet, partIdx, tile.getPos());
                    packet.sendToPlayer(player);
                });
    }
    //endregion

    //region Send C_PART_UPDATE
    public static void dispatchPartUpdate(TMultiPart part, Consumer<MCDataOutput> func) {
        ServerWorld world = (ServerWorld) part.world();
        MCByteStream stream = new MCByteStream();
        func.accept(stream);
        BlockPos pos = part.pos();
        world.getChunkProvider().chunkManager.getTrackingPlayers(new ChunkPos(pos), false)//
                .forEach(player -> {
                    PacketCustom packet = new PacketCustom(NET_CHANNEL, C_PART_UPDATE);
                    writeHeaderFor(player, packet, part.tile().jPartList().indexOf(part), part.pos());
                    packet.append(stream.getBytes());
                    packet.sendToPlayer(player);
                });
    }
    //endregion

    //region Helpers.
    private static void writeHeaderFor(ServerPlayerEntity player, MCDataOutput packet, int partId, BlockPos pos) {
        BlockPos last = playerLastPositions.computeIfAbsent(player.getUniqueID(), id -> new BlockPos(0, 0, 0));
        writeHeader(packet, partId, last, pos);
        playerLastPositions.put(player.getUniqueID(), pos);
    }

    /**
     * Writes a Runtime Update packet's header.
     * <p>
     * The packet can be in 4 separate states.<br/>
     * For all states, a single byte is prefixed, the first 6 bits containing the {@link TMultiPart} partIndex, followed by 2 Flag bits.<br/>
     * The 2 flag bytes determine what state the packet is in.<br/>
     * State 0x00, Full, Writes the BlockPos using signedVarInt's.<br/>
     * State 0x01, SameBlock. Doesnt write any coords, client remembers.<br/>
     * State 0x02, SubChunk relative, Writes the 16x256x16, sub chunk coords of the block into 2 bytes, the upper 4 bits of byte1 containing
     * the X coords, and the lower 4 of byte1 containing the Z coords and byte2 containing the Y coords.<br/>
     * State 0x03, Within -128 - 127 relative. Relative coords to the last block, within 128 blocks on any axis of the last block.<br/>
     * <p>
     * Is this over-engineered? Quite possibly.
     */
    @VisibleForTesting
    static void writeHeader(MCDataOutput out, int partIdx, BlockPos last, BlockPos current) {
        boolean sameBlock = last.equals(current);
        if (sameBlock) {//Same blockspace, write no position.
            out.writeByte((partIdx << 2) | 0x01);
            return;
        }

        boolean sameChunk = last.getX() >> 4 == current.getX() >> 4 && last.getZ() >> 4 == current.getZ() >> 4 && last.getY() >> 8 == current.getY() >> 8;
        if (sameChunk) {//Within the same 16x256x16 SubChunk, pack sub chunk coords into 2 bytes: XXXXZZZZ YYYYYYYY
            out.writeByte((partIdx << 2) | 0x02);
            out.writeByte((current.getX() & 0xF) << 4 | (current.getZ() & 0xF));
            out.writeByte(current.getY() & 0xFF);
            return;
        }

        boolean within128 = between(-128, current.getX() - last.getX(), 127) && //
                between(-128, current.getY() - last.getY(), 127) && //
                between(-128, current.getZ() - last.getZ(), 127);
        if (within128) {//Within -128 - 127 blocks on any axis.
            out.writeByte((partIdx << 2) | 0x03);
            out.writeByte(current.getX() - last.getX());
            out.writeByte(current.getY() - last.getY());
            out.writeByte(current.getZ() - last.getZ());
            return;
        }

        //Send the entire BlockPos using signedVarInt's
        out.writeByte((partIdx << 2)/* | 0x00*/);
        out.writePos(current);
    }
    //endregion

    //region Events
    //I don't trust the LoggedOut event.
    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        playerLastPositions.remove(event.getPlayer().getUniqueID());
    }

    private static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        playerLastPositions.remove(event.getPlayer().getUniqueID());
    }
    //endregion
}
