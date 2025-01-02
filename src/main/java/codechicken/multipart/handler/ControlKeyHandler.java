package codechicken.multipart.handler;

import codechicken.lib.packet.PacketCustom;
import codechicken.multipart.network.MultiPartNetwork;
import codechicken.multipart.util.ControlKeyModifier;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

/**
 * Created by covers1624 on 1/9/20.
 */
public class ControlKeyHandler {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");
    private static final KeyMapping KEY = new KeyMapping("key.control", GLFW.GLFW_KEY_LEFT_CONTROL, "key.categories.gameplay");

    private static boolean lastPressed = false;

    public static void init(IEventBus modBus) {
        LOCK.lock();
        modBus.addListener(ControlKeyHandler::register);

        NeoForge.EVENT_BUS.addListener(ControlKeyHandler::tick);
    }

    private static void register(RegisterKeyMappingsEvent event) {
        event.register(KEY);
    }

    private static void tick(ClientTickEvent.Post event) {
        boolean pressed = KEY.isDown();
        if (pressed != lastPressed) {
            lastPressed = pressed;
            Minecraft mc = Minecraft.getInstance();
            if (mc.getConnection() != null) {
                ControlKeyModifier.setIsControlDown(mc.player, pressed);
                PacketCustom packet = new PacketCustom(MultiPartNetwork.NET_CHANNEL, MultiPartNetwork.S_CONTROL_KEY_MODIFIER, mc.player.registryAccess());
                packet.writeBoolean(pressed);
                packet.sendToServer();
            }
        }
    }

}
