package codechicken.multipart.capability;

import net.minecraftforge.common.capabilities.Capability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static codechicken.lib.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 3/1/21.
 */
public class CapabilityMerger {

    private static final Map<Capability<?>, Function<List<?>, ?>> capabilityMergers = new HashMap<>();

    public static <T> void addMerger(Capability<T> cap, Function<List<T>, T> merger) {
        capabilityMergers.put(cap, unsafeCast(merger));
    }

    public static <T> T merge(Capability<T> cap, List<T> list) {
        Function<List<T>, T> func = unsafeCast(capabilityMergers.get(cap));
        if (func == null) {
            return list.get(0);
        }
        return func.apply(list);
    }
}
