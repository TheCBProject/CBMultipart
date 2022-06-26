package codechicken.multipart.network;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.util.MultiPartHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;

import static codechicken.multipart.network.MultiPartNetwork.*;

/**
 * Created by covers1624 on 4/30/20.
 */
public class MultiPartCPH implements ICustomPacketHandler.IClientPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, ClientPacketListener handler) {
        switch (packet.getType()) {
            case C_TILE_DESC: {
                handleTileDescPacket(packet, mc);
                break;
            }
            case C_ADD_PART: {
                handleAddPart(packet, mc);
                break;
            }
            case C_REM_PART: {
                handleRemPart(packet, mc);
                break;
            }
            case C_PART_UPDATE: {
                handleUpdatePacket(packet, mc);
                break;
            }
        }
    }

    public static void handleTileDescPacket(MCDataInput packet, Minecraft mc) {
        int num = packet.readVarInt();
        for (int i = 0; i < num; i++) {
            BlockPos pos = packet.readPos();
            TileMultiPart.handleDescPacket(mc.level, pos, packet);
        }
    }

    public static void handleAddPart(MCDataInput packet, Minecraft mc) {
        BlockPos pos = packet.readPos();
        MultiPartHelper.addPart(mc.level, pos, MultiPartRegistries.readPart(packet));
    }

    public static void handleRemPart(MCDataInput packet, Minecraft mc) {
        byte partIndex = packet.readByte();
        BlockPos pos = packet.readPos();
        if (mc.level.getBlockEntity(pos) instanceof TileMultiPart tile) {
            tile.remPart_impl(tile.getPartList().get(partIndex));
        }
    }

    public static void handleUpdatePacket(MCDataInput packet, Minecraft mc) {
        int partIndex = packet.readByte();
        BlockPos pos = packet.readPos();
        if (mc.level.getBlockEntity(pos) instanceof TileMultiPart tile) {
            TMultiPart part = tile.getPartList().get(partIndex);
            if (part != null) {
                part.readUpdate(packet);
            }
        }
    }
}
