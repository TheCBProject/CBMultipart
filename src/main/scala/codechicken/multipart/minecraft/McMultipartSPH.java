//package codechicken.multipart.minecraft;
//
//import codechicken.lib.packet.ICustomPacketHandler.IServerPacketHandler;
//import codechicken.lib.packet.PacketCustom;
//import net.minecraft.entity.player.ServerPlayerEntity;
//import net.minecraft.network.play.IServerPlayNetHandler;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;
//
//public class McMultipartSPH implements IServerPacketHandler {
//
//    public static ResourceLocation channel = new ResourceLocation("mcmultipart_cbe:network");
//
//    @Override
//    public void handlePacket(PacketCustom packet, ServerPlayerEntity sender, IServerPlayNetHandler netHandler) {
//        switch (packet.getType()) {
//        }
//    }
//
//    public static void spawnBurnoutSmoke(World world, BlockPos pos) {
//        new PacketCustom(channel, 1).writePos(pos).sendToChunk(world, pos.getX() >> 4, pos.getZ() >> 4);
//    }
//}
