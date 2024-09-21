package codechicken.multipart.network;

import codechicken.lib.packet.PacketCustomChannel;
import codechicken.multipart.CBMultipart;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;

/**
 * Created by covers1624 on 4/30/20.
 */
public class MultiPartNetwork {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");
    public static final ResourceLocation NET_CHANNEL = new ResourceLocation(CBMultipart.MOD_ID, "network");
    public static final PacketCustomChannel channel = new PacketCustomChannel(NET_CHANNEL)
            .versioned(CBMultipart.container().getModInfo().getVersion().toString())
            .client(() -> MultiPartCPH::new)
            .server(() -> MultiPartSPH::new);

    //Client handled.
    public static final int C_TILE_DESC = 1;
    public static final int C_ADD_PART = 2;
    public static final int C_REM_PART = 3;
    public static final int C_PART_UPDATE = 4;

    public static final int C_LANDING_EFFECTS = 10;

    //Server handled.
    public static final int S_CONTROL_KEY_MODIFIER = 1;

    public static void init(IEventBus modBus) {
        LOCK.lock();
        channel.init(modBus);
    }

}
