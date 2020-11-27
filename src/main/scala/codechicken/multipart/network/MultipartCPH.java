package codechicken.multipart.network;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.multipart.util.MultiPartHelper;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.api.part.TMultiPart;
import com.google.common.annotations.VisibleForTesting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;

import static codechicken.multipart.network.MultipartNetwork.*;

/**
 * Created by covers1624 on 4/30/20.
 */
public class MultipartCPH implements ICustomPacketHandler.IClientPacketHandler {

    @VisibleForTesting
    static BlockPos lastPos = new BlockPos(0, 0, 0);

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(MultipartCPH::onDisconnectFromServer);
        MinecraftForge.EVENT_BUS.addListener(MultipartCPH::onConnectToServer);
    }

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
            BlockPos pos = readUpdateHeader(packet.readByte() & 0x03, packet);
            TileMultipart.handleDescPacket(mc.world, pos, packet);
        }
    }

    public static void handleAddPart(MCDataInput packet, Minecraft mc) {
        BlockPos pos = readUpdateHeader(packet.readByte() & 0x3, packet);
        MultiPartHelper.addPart(mc.world, pos, MultiPartRegistries.readPart(packet));
    }

    public static void handleRemPart(MCDataInput packet, Minecraft mc) {
        byte partByte = packet.readByte();
        BlockPos pos = readUpdateHeader(partByte & 0x3, packet);
        TileEntity tileEntity = mc.world.getTileEntity(pos);
        if (tileEntity instanceof TileMultipart) {
            TileMultipart tile = (TileMultipart) tileEntity;
            tile.remPart_impl(tile.jPartList().get(partByte >> 2));
        }
    }

    public static void handleUpdatePacket(MCDataInput packet, Minecraft mc) {
        byte partByte = packet.readByte();
        int partIndex = partByte >> 2;
        BlockPos pos = readUpdateHeader(partByte & 0x03, packet);
        TileEntity tileEntity = mc.world.getTileEntity(pos);
        if (tileEntity instanceof TileMultipart) {
            TileMultipart tile = (TileMultipart) tileEntity;
            TMultiPart part = tile.jPartList().get(partIndex);
            if (part != null) {
                part.readUpdate(packet);
            }
        }
    }

    @VisibleForTesting
    static BlockPos readUpdateHeader(int flags, MCDataInput packet) {
        BlockPos pos;
        switch (flags) {
            case 0x00: {
                pos = packet.readPos();
                break;
            }
            case 0x01: {
                pos = lastPos;
                break;
            }
            case 0x02: {
                byte xzByte = packet.readByte();
                byte yByte = packet.readByte();
                pos = new BlockPos(//
                        lastPos.getX() & ~(0xF) | (xzByte >> 4 & 0xF),//
                        lastPos.getY() & ~(0xFF) | yByte,//
                        lastPos.getZ() & ~(0xF) | xzByte & 0xF//
                );
                break;
            }
            case 0x03: {
                pos = lastPos.add(packet.readByte(), packet.readByte(), packet.readByte());
                break;
            }
            default:
                throw new RuntimeException("What? " + Integer.toBinaryString(flags));
        }
        lastPos = pos;
        return pos;
    }

    private static void onDisconnectFromServer(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        lastPos = BlockPos.ZERO;
    }
    private static void onConnectToServer(ClientPlayerNetworkEvent.LoggedInEvent event) {
        lastPos = BlockPos.ZERO;
    }
}
