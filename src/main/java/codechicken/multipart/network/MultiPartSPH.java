package codechicken.multipart.network;

import codechicken.lib.data.MCByteStream;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.util.LazyValuePair;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.util.ControlKeyModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static codechicken.multipart.network.MultiPartNetwork.*;

/**
 * Created by covers1624 on 4/30/20.
 */
public class MultiPartSPH implements ICustomPacketHandler.IServerPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, ServerPlayer sender, ServerGamePacketListenerImpl handler) {
        switch (packet.getType()) {
            case S_CONTROL_KEY_MODIFIER -> ControlKeyModifier.setIsControlDown(sender, packet.readBoolean());
        }
    }

    //region Send C_TILE_DESC
    public static void sendDescUpdate(TileMultipart tile) {
        ServerLevel world = (ServerLevel) tile.getLevel();
        List<ServerPlayer> players = world.getChunkSource().chunkMap.getPlayers(new ChunkPos(tile.getBlockPos()), false);
        sendDescUpdates(players, Collections.singleton(tile));
    }

    public static void sendDescUpdates(List<ServerPlayer> players, Collection<BlockEntity> tiles) {
        if (tiles.isEmpty()) return;

        List<Pair<TileMultipart, MCByteStream>> data = new LinkedList<>();
        for (BlockEntity tile : tiles) {
            if (!(tile instanceof TileMultipart partTile)) continue;
            data.add(LazyValuePair.of(partTile, t -> {
                MCByteStream stream = new MCByteStream();
                t.writeDesc(stream);
                return stream;
            }));
        }
        if (data.isEmpty()) return;

        for (ServerPlayer player : players) {
            sendDescUpdateTo(player, data);
        }
    }

    private static void sendDescUpdateTo(ServerPlayer player, List<Pair<TileMultipart, MCByteStream>> data) {
        PacketCustom packet = new PacketCustom(NET_CHANNEL, C_TILE_DESC);
        packet.writeVarInt(data.size());
        for (Pair<TileMultipart, MCByteStream> entry : data) {
            packet.writePos(entry.getLeft().getBlockPos());
            packet.append(entry.getRight().getBytes());
        }
        packet.sendToPlayer(player);
    }
    //endregion

    //region Send C_ADD_PART
    public static void sendAddPart(TileMultipart tile, MultiPart part) {
        ServerLevel world = (ServerLevel) tile.getLevel();
        MCByteStream stream = new MCByteStream();
        MultiPartRegistries.writePart(stream, part);
        world.getChunkSource().chunkMap.getPlayers(new ChunkPos(tile.getBlockPos()), false)
                .forEach(player -> {
                    PacketCustom packet = new PacketCustom(NET_CHANNEL, C_ADD_PART);
                    packet.writePos(tile.getBlockPos());
                    packet.append(stream.getBytes());
                    packet.sendToPlayer(player);
                });
    }
    //endregion

    //region Send C_REM_PART
    public static void sendRemPart(TileMultipart tile, int partIdx) {
        ServerLevel world = (ServerLevel) tile.getLevel();
        world.getChunkSource().chunkMap.getPlayers(new ChunkPos(tile.getBlockPos()), false)
                .forEach(player -> {
                    PacketCustom packet = new PacketCustom(NET_CHANNEL, C_REM_PART);
                    packet.writeByte(partIdx);
                    packet.writePos(tile.getBlockPos());
                    packet.sendToPlayer(player);
                });
    }
    //endregion

    //region Send C_PART_UPDATE
    public static void dispatchPartUpdate(MultiPart part, Consumer<MCDataOutput> func) {
        ServerLevel world = (ServerLevel) part.level();
        MCByteStream stream = new MCByteStream();
        func.accept(stream);
        BlockPos pos = part.pos();
        world.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false)
                .forEach(player -> {
                    PacketCustom packet = new PacketCustom(NET_CHANNEL, C_PART_UPDATE);
                    packet.writeByte(part.tile().getPartList().indexOf(part));
                    packet.writePos(part.pos());
                    packet.append(stream.getBytes());
                    packet.sendToPlayer(player);
                });
    }
    //endregion
}
