package codechicken.multipart.network;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.util.MultipartHelper;
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
            case C_TILE_DESC -> handleTileDescPacket(packet, mc);
            case C_ADD_PART -> handleAddPart(packet, mc);
            case C_REM_PART -> handleRemPart(packet, mc);
            case C_PART_UPDATE -> handleUpdatePacket(packet, mc);
            case C_LANDING_EFFECTS -> handleLandingEffects(packet, mc);
        }
    }

    public static void handleTileDescPacket(MCDataInput packet, Minecraft mc) {
        int num = packet.readVarInt();
        for (int i = 0; i < num; i++) {
            BlockPos pos = packet.readPos();
            TileMultipart.handleDescPacket(mc.level, pos, packet);
        }
    }

    public static void handleAddPart(MCDataInput packet, Minecraft mc) {
        BlockPos pos = packet.readPos();
        MultipartHelper.addPart(mc.level, pos, MultiPartRegistries.readPart(packet));
    }

    public static void handleRemPart(MCDataInput packet, Minecraft mc) {
        byte partIndex = packet.readByte();
        BlockPos pos = packet.readPos();
        if (mc.level.getBlockEntity(pos) instanceof TileMultipart tile) {
            tile.remPart_impl(tile.getPartList().get(partIndex));
        }
    }

    public static void handleUpdatePacket(MCDataInput packet, Minecraft mc) {
        int partIndex = packet.readByte();
        BlockPos pos = packet.readPos();
        if (mc.level.getBlockEntity(pos) instanceof TileMultipart tile) {
            MultiPart part = tile.getPartList().get(partIndex);
            if (part != null) {
                part.readUpdate(packet);
            }
        }
    }

    private void handleLandingEffects(PacketCustom packet, Minecraft mc) {
        BlockPos pos = packet.readPos();
        if (mc.level.getBlockEntity(pos) instanceof TileMultipart tile) {
            tile.addLandingEffects(packet.readVector(), packet.readVarInt());
        }
    }
}
