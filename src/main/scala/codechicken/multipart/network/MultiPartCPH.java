package codechicken.multipart.network;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.util.MultiPartHelper;
import com.google.common.annotations.VisibleForTesting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;

import static codechicken.multipart.network.MultiPartNetwork.*;

/**
 * Created by covers1624 on 4/30/20.
 */
public class MultiPartCPH implements ICustomPacketHandler.IClientPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, IClientPlayNetHandler handler) {
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
        TileEntity tileEntity = mc.level.getBlockEntity(pos);
        if (tileEntity instanceof TileMultiPart) {
            TileMultiPart tile = (TileMultiPart) tileEntity;
            tile.remPart_impl(tile.getPartList().get(partIndex));
        }
    }

    public static void handleUpdatePacket(MCDataInput packet, Minecraft mc) {
        int partIndex = packet.readByte();
        BlockPos pos = packet.readPos();
        TileEntity tileEntity = mc.level.getBlockEntity(pos);
        if (tileEntity instanceof TileMultiPart) {
            TileMultiPart tile = (TileMultiPart) tileEntity;
            TMultiPart part = tile.getPartList().get(partIndex);
            if (part != null) {
                part.readUpdate(packet);
            }
        }
    }
}
