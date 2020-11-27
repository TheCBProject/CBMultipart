package codechicken.microblock.handler

import java.util.function.{BiConsumer, Supplier}

import codechicken.lib.packet.ICustomPacketHandler.{IClientPacketHandler, ILoginPacketHandler, IServerPacketHandler}
import codechicken.lib.packet.{PacketCustom, PacketCustomChannelBuilder}
import codechicken.microblock.MicroMaterialRegistry
import codechicken.microblock.handler.MicroblockNetwork._
import net.minecraft.client.Minecraft
import net.minecraft.client.network.login.IClientLoginNetHandler
import net.minecraft.client.network.play.IClientPlayNetHandler
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.login.server.SDisconnectLoginPacket
import net.minecraft.network.play.IServerPlayNetHandler
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.StringTextComponent
import net.minecraftforge.fml.network.NetworkEvent
import net.minecraftforge.fml.network.event.EventNetworkChannel

object MicroblockNetwork {
    val CHANNEL = new ResourceLocation("microblock_cbe:network")
    var netChannel: EventNetworkChannel = _

    def init() {
        netChannel = PacketCustomChannelBuilder.named(CHANNEL)
            .networkProtocolVersion(() => "1")
            .serverAcceptedVersions(_ => true)
            .clientAcceptedVersions(_ => true)
            .assignClientHandler(() => () => MicroblockCPH)
            .assignServerHandler(() => () => MicroblockSPH)
            .assignLoginHandler(() => () => MicroblockLPH)
            .build()
    }

}

object MicroblockCPH extends IClientPacketHandler {
    def handlePacket(packet: PacketCustom, mc: Minecraft, netHandler: IClientPlayNetHandler) {}
}

object MicroblockSPH extends IServerPacketHandler {
    def handlePacket(packet: PacketCustom, sender: ServerPlayerEntity, netHandler: IServerPlayNetHandler) {}
}

object MicroblockLPH extends ILoginPacketHandler {
    override def gatherLoginPackets(consumer: BiConsumer[String, Supplier[PacketCustom]]) {}

    override def handleLoginPacket(packet: PacketCustom, mc: Minecraft, handler: IClientLoginNetHandler, context: NetworkEvent.Context) {}
}
