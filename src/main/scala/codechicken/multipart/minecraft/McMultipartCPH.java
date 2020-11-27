//package codechicken.multipart.minecraft;
//
//import codechicken.lib.packet.ICustomPacketHandler.IClientPacketHandler;
//import codechicken.lib.packet.PacketCustom;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.network.play.IClientPlayNetHandler;
//import net.minecraft.client.world.ClientWorld;
//import net.minecraft.particles.ParticleTypes;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.math.BlockPos;
//
//public class McMultipartCPH implements IClientPacketHandler {
//
//    public static ResourceLocation channel = new ResourceLocation("mcmultipart_cbe:network");
//
//    @Override
//    public void handlePacket(PacketCustom packet, Minecraft mc, IClientPlayNetHandler netHandler) {
//        switch (packet.getType()) {
//            case 1:
//                spawnBurnoutSmoke(mc.world, packet.readPos());
//                break;
//        }
//    }
//
//    private void spawnBurnoutSmoke(ClientWorld world, BlockPos pos) {
//        for (int l = 0; l < 5; l++) {
//            world.addParticle(ParticleTypes.SMOKE, pos.getX() + world.rand.nextDouble() * 0.6 + 0.2, pos.getY() + world.rand.nextDouble() * 0.6 + 0.2, pos.getZ() + world.rand.nextDouble() * 0.6 + 0.2, 0, 0, 0);
//        }
//    }
//}
