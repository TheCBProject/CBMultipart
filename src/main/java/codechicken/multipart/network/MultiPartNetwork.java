package codechicken.multipart.network;

import codechicken.lib.packet.PacketCustomChannelBuilder;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.event.EventNetworkChannel;

/**
 * Created by covers1624 on 4/30/20.
 */
public class MultiPartNetwork {

    public static final ResourceLocation NET_CHANNEL = new ResourceLocation("cmp:n");
    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    //Client handled.
    public static final int C_TILE_DESC = 1;
    public static final int C_ADD_PART = 2;
    public static final int C_REM_PART = 3;
    public static final int C_PART_UPDATE = 4;

    public static final int C_LANDING_EFFECTS = 10;

    //Server handled.
    public static final int S_CONTROL_KEY_MODIFIER = 1;
    public static final int S_MULTIPART_PLACEMENT = 10;

    public static EventNetworkChannel netChannel;

    public static void init() {
        LOCK.lock();
        netChannel = PacketCustomChannelBuilder.named(NET_CHANNEL)
                .assignServerHandler(() -> MultiPartSPH::new)
                .assignClientHandler(() -> MultiPartCPH::new)
                .build();
    }

}
