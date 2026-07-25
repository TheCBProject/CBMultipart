package codechicken.multipart.network;

import codechicken.lib.packet.StreamNetworkChannel;
import codechicken.lib.packet.StreamNetworkChannel.ClientPacketHandle;
import codechicken.lib.packet.StreamNetworkChannel.ServerPacketHandle;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.util.ControlKeyModifier;
import codechicken.multipart.util.MultipartHelper;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Consumer;

import static codechicken.multipart.CBMultipart.MOD_ID;

/**
 * Created by covers1624 on 4/30/20.
 */
public class MultiPartNetwork {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");
    public static final StreamNetworkChannel CHANNEL = new StreamNetworkChannel(MOD_ID);

    public static final ClientPacketHandle TILE_DESC = CHANNEL.playToClient("tile_desc", MultiPartNetwork::handleTileDesc);
    public static final ClientPacketHandle ADD_PART = CHANNEL.playToClient("add_part", MultiPartNetwork::handleAddPart);
    public static final ClientPacketHandle REM_PART = CHANNEL.playToClient("rem_part", MultiPartNetwork::handleRemPart);
    public static final ClientPacketHandle PART_UPDATE = CHANNEL.playToClient("part_update", MultiPartNetwork::handlePartUpdate);
    public static final ClientPacketHandle LANDING_EFFECTS = CHANNEL.playToClient("landing_effects", MultiPartNetwork::handleLandingEffects);

    public static final ServerPacketHandle CONTROL_KEY_MODIFIER = CHANNEL.playToServer("ctrl_key", MultiPartNetwork::handleControlKeyModifier);

    public static void init(IEventBus modBus, ModContainer container) {
        LOCK.lock();
        CHANNEL.init(modBus, container);
    }

    public static void sendDescUpdate(TileMultipart tile) {
        var packet = TILE_DESC.toClient(tile);
        packet.writeBlockPos(tile.getBlockPos());
        tile.writeDesc(packet);
        packet.sendToChunk(tile);
    }

    private static void handleTileDesc(RegistryFriendlyByteBuf packet, IPayloadContext ctx) {
        var pos = packet.readBlockPos();
        TileMultipart.handleDescPacket(ctx.player().level(), pos, packet);
    }

    public static void sendAddPart(TileMultipart tile, MultiPart part) {
        var packet = ADD_PART.toClient(tile);
        packet.writeBlockPos(tile.getBlockPos());
        MultiPartRegistries.writePart(packet, part);
        packet.sendToChunk(tile);
    }

    private static void handleAddPart(RegistryFriendlyByteBuf packet, IPayloadContext ctx) {
        BlockPos pos = packet.readBlockPos();
        MultipartHelper.addPart(ctx.player().level(), pos, MultiPartRegistries.readPart(packet));
    }

    public static void sendRemPart(TileMultipart tile, int partIdx) {
        var packet = REM_PART.toClient(tile);
        packet.writeBlockPos(tile.getBlockPos());
        packet.writeByte(partIdx);
        packet.sendToChunk(tile);
    }

    private static void handleRemPart(RegistryFriendlyByteBuf packet, IPayloadContext ctx) {
        BlockPos pos = packet.readBlockPos();
        byte partIndex = packet.readByte();
        if (ctx.player().level().getBlockEntity(pos) instanceof TileMultipart tile) {
            tile.remPart_impl(tile.getPartList().get(partIndex));
        }
    }

    public static void sendPartUpdate(MultiPart part, Consumer<RegistryFriendlyByteBuf> func) {
        var packet = PART_UPDATE.toClient(part.tile());
        packet.writeBlockPos(part.pos());
        packet.writeByte(part.tile().getPartList().indexOf(part));
        func.accept(packet);
        packet.sendToChunk(part.tile());
    }

    private static void handlePartUpdate(RegistryFriendlyByteBuf packet, IPayloadContext ctx) {
        BlockPos pos = packet.readBlockPos();
        int partIndex = packet.readByte();
        if (ctx.player().level().getBlockEntity(pos) instanceof TileMultipart tile) {
            MultiPart part = tile.getPartList().get(partIndex);
            if (part != null) {
                part.readUpdate(packet);
            }
        }
    }

    private static void handleLandingEffects(RegistryFriendlyByteBuf packet, IPayloadContext ctx) {
        BlockPos pos = packet.readBlockPos();
        if (ctx.player().level().getBlockEntity(pos) instanceof TileMultipart tile) {
            tile.addLandingEffects(packet.readVector3(), packet.readVarInt());
        }
    }

    private static void handleControlKeyModifier(RegistryFriendlyByteBuf packet, IPayloadContext ctx) {
        ControlKeyModifier.setIsControlDown(ctx.player(), packet.readBoolean());
    }
}
