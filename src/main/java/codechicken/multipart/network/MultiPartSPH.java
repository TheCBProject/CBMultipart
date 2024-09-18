package codechicken.multipart.network;

import codechicken.lib.data.MCByteStream;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.util.ControlKeyModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.function.Consumer;

import static codechicken.multipart.network.MultiPartNetwork.*;

/**
 * Created by covers1624 on 4/30/20.
 */
public class MultiPartSPH implements ICustomPacketHandler.IServerPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, ServerPlayer sender) {
        switch (packet.getType()) {
            case S_CONTROL_KEY_MODIFIER -> ControlKeyModifier.setIsControlDown(sender, packet.readBoolean());
        }
    }

    //region Send C_TILE_DESC
    public static void sendDescUpdate(TileMultipart tile) {
        PacketCustom packet = new PacketCustom(NET_CHANNEL, C_TILE_DESC);
        packet.writePos(tile.getBlockPos());
        tile.writeDesc(packet);
        packet.sendToChunk(tile);
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
