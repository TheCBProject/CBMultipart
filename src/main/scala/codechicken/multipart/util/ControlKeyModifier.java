package codechicken.multipart.util;

import net.minecraft.entity.player.PlayerEntity;

import java.util.WeakHashMap;

/**
 * Created by covers1624 on 1/9/20.
 */
public class ControlKeyModifier {

    private static final WeakHashMap<PlayerEntity, Boolean> playerMap = new WeakHashMap<>();

    public static void setIsControlDown(PlayerEntity player, boolean bool) {
        playerMap.put(player, bool);
    }

    public static boolean isControlDown(PlayerEntity player) {
        Boolean bool = playerMap.get(player);
        return bool != null && bool;
    }
}
