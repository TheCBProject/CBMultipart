package codechicken.multipart.network;

import codechicken.lib.data.MCByteStream;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.util.CrashLock;
import codechicken.lib.util.LazyValuePair;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.handler.PlacementConversionHandler;
import codechicken.multipart.init.MultiPartRegistries;
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
import static codechicken.multipart.network.MultiPartNetwork.*;

/**
 * Created by covers1624 on 4/30/20.
 */
public class MultiPartSPH implements ICustomPacketHandler.IServerPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, ServerPlayerEntity sender, IServerPlayNetHandler handler) {
        switch (packet.getType()) {
            case S_CONTROL_KEY_MODIFIER: {
                ControlKeyModifier.setIsControlDown(sender, packet.readBoolean());
                break;
            }
            case S_MULTIPART_PLACEMENT: {
                PlacementConversionHandler.place(sender, packet.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND, sender.level);
                break;
            }
        }
    }

    //region Send C_TILE_DESC
    public static void sendDescUpdate(TileMultiPart tile) {
        ServerWorld world = (ServerWorld) tile.getLevel();
        Stream<ServerPlayerEntity> players = world.getChunkSource().chunkMap.getPlayers(new ChunkPos(tile.getBlockPos()), false);
        sendDescUpdates(players, Collections.singleton(tile));
    }

    public static void sendDescUpdates(Stream<ServerPlayerEntity> players, Collection<TileEntity> tiles) {
        if (tiles.isEmpty()) {
            return;
        }
        List<Pair<TileMultiPart, MCByteStream>> data = tiles.stream()
                .filter(e -> e instanceof TileMultiPart)
                .map(e -> (TileMultiPart) e)
                .map(tile -> LazyValuePair.of(tile, t -> {
                    MCByteStream stream = new MCByteStream();
                    t.writeDesc(stream);
                    return stream;
                }))
                .collect(Collectors.toList());
        if (data.isEmpty()) {
            return;
        }
        players.forEach(player -> sendDescUpdateTo(player, data));
    }

    private static void sendDescUpdateTo(ServerPlayerEntity player, List<Pair<TileMultiPart, MCByteStream>> data) {
        PacketCustom packet = new PacketCustom(NET_CHANNEL, C_TILE_DESC);
        packet.writeVarInt(data.size());
        for (Pair<TileMultiPart, MCByteStream> entry : data) {
            packet.writePos(entry.getLeft().getBlockPos());
            packet.append(entry.getRight().getBytes());
        }
        packet.sendToPlayer(player);
    }
    //endregion

    //region Send C_ADD_PART
    public static void sendAddPart(TileMultiPart tile, TMultiPart part) {
        ServerWorld world = (ServerWorld) tile.getLevel();
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
    public static void sendRemPart(TileMultiPart tile, int partIdx) {
        ServerWorld world = (ServerWorld) tile.getLevel();
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
    public static void dispatchPartUpdate(TMultiPart part, Consumer<MCDataOutput> func) {
        ServerWorld world = (ServerWorld) part.world();
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
