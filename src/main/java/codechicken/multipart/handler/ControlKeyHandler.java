package codechicken.multipart.handler;

import codechicken.lib.packet.PacketCustom;
import codechicken.multipart.network.MultiPartNetwork;
import codechicken.multipart.util.ControlKeyModifier;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
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

    private static void tick(TickEvent.ClientTickEvent event) {
        boolean pressed = KEY.isDown();
        if (pressed != lastPressed) {
            lastPressed = pressed;
            if (Minecraft.getInstance().getConnection() != null) {
                ControlKeyModifier.setIsControlDown(Minecraft.getInstance().player, pressed);
                PacketCustom packet = new PacketCustom(MultiPartNetwork.NET_CHANNEL, MultiPartNetwork.S_CONTROL_KEY_MODIFIER);
                packet.writeBoolean(pressed);
                packet.sendToServer();
            }
        }
    }

}
