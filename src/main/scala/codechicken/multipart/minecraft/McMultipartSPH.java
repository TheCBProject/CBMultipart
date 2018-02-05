package codechicken.multipart.minecraft;

import codechicken.lib.packet.ICustomPacketHandler.IServerPacketHandler;
import codechicken.lib.packet.PacketCustom;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class McMultipartSPH implements IServerPacketHandler {

	public static Object channel = "mcmultipart_cbe";

	@Override
	public void handlePacket(PacketCustom packet, EntityPlayerMP sender, INetHandlerPlayServer netHandler) {
		switch (packet.getType()) {
			case 1:
				EventHandler.place(sender, packet.readBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND, sender.world);
				break;
		}
	}

	public static void spawnBurnoutSmoke(World world, BlockPos pos) {
		new PacketCustom(channel, 1).writePos(pos).sendToChunk(world, pos.getX() >> 4, pos.getZ() >> 4);
	}
}
