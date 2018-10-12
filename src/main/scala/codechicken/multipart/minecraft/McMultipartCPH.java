package codechicken.multipart.minecraft;

import codechicken.lib.packet.ICustomPacketHandler.IClientPacketHandler;
import codechicken.lib.packet.PacketCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;

public class McMultipartCPH implements IClientPacketHandler {

    public static Object channel = "mcmultipart_cbe";

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, INetHandlerPlayClient netHandler) {
        switch (packet.getType()) {
            case 1:
                spawnBurnoutSmoke(mc.world, packet.readPos());
                break;
        }
    }

    private void spawnBurnoutSmoke(WorldClient world, BlockPos pos) {
        for (int l = 0; l < 5; l++) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + world.rand.nextDouble() * 0.6 + 0.2, pos.getY() + world.rand.nextDouble() * 0.6 + 0.2, pos.getZ() + world.rand.nextDouble() * 0.6 + 0.2, 0, 0, 0);
        }
    }
}
