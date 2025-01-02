package codechicken.multipart.network;

import codechicken.lib.data.MCDataOutput;
import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.util.ControlKeyModifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

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
        PacketCustom packet = new PacketCustom(NET_CHANNEL, C_TILE_DESC, tile.getLevel().registryAccess());
        packet.writePos(tile.getBlockPos());
        tile.writeDesc(packet);
        packet.sendToChunk(tile);
    }
    //endregion

    //region Send C_ADD_PART
    public static void sendAddPart(TileMultipart tile, MultiPart part) {
        ServerLevel world = (ServerLevel) tile.getLevel();
        PacketCustom packet = new PacketCustom(NET_CHANNEL, C_ADD_PART, world.registryAccess());
        packet.writePos(tile.getBlockPos());
        MultiPartRegistries.writePart(packet, part);
        packet.sendToChunk(tile);
    }
    //endregion

    //region Send C_REM_PART
    public static void sendRemPart(TileMultipart tile, int partIdx) {
        PacketCustom packet = new PacketCustom(NET_CHANNEL, C_REM_PART, tile.getLevel().registryAccess());
        packet.writeByte(partIdx);
        packet.writePos(tile.getBlockPos());
        packet.sendToChunk(tile);
    }
    //endregion

    //region Send C_PART_UPDATE
    public static void dispatchPartUpdate(MultiPart part, Consumer<MCDataOutput> func) {
        ServerLevel world = (ServerLevel) part.level();
        PacketCustom packet = new PacketCustom(NET_CHANNEL, C_PART_UPDATE, world.registryAccess());
        packet.writeByte(part.tile().getPartList().indexOf(part));
        packet.writePos(part.pos());
        func.accept(packet);
        packet.sendToChunk(part.tile());
    }
    //endregion
}
