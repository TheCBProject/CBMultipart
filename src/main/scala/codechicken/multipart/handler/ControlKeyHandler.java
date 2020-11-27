package codechicken.multipart.handler;

import codechicken.lib.packet.PacketCustom;
import codechicken.lib.util.CrashLock;
import codechicken.multipart.network.MultipartNetwork;
import codechicken.multipart.util.ControlKeyModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

/**
 * Created by covers1624 on 1/9/20.
 */
public class ControlKeyHandler {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");
    private static final KeyBinding KEY = new KeyBinding("key.control", GLFW.GLFW_KEY_LEFT_CONTROL, "key.categories.gameplay");

    private static boolean lastPressed = false;

    public static void init() {
        LOCK.lock();
        MinecraftForge.EVENT_BUS.addListener(ControlKeyHandler::tick);
        ClientRegistry.registerKeyBinding(KEY);
    }

    private static void tick(TickEvent.ClientTickEvent event) {
        boolean pressed = KEY.isKeyDown();
        if (pressed != lastPressed) {
            lastPressed = pressed;
            if (Minecraft.getInstance().getConnection() != null) {
                ControlKeyModifier.setIsControlDown(Minecraft.getInstance().player, pressed);
                PacketCustom packet = new PacketCustom(MultipartNetwork.NET_CHANNEL, MultipartNetwork.S_CONTROL_KEY_MODIFIER);
                packet.writeBoolean(pressed);
                packet.sendToServer();
            }
        }
    }

}
