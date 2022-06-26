package codechicken.multipart.util;

import net.minecraft.world.entity.player.Player;

import java.util.WeakHashMap;

/**
 * Created by covers1624 on 1/9/20.
 */
public class ControlKeyModifier {

    private static final WeakHashMap<Player, Boolean> playerMap = new WeakHashMap<>();

    public static void setIsControlDown(Player player, boolean bool) {
        playerMap.put(player, bool);
    }

    public static boolean isControlDown(Player player) {
        Boolean bool = playerMap.get(player);
        return bool != null && bool;
    }
}
