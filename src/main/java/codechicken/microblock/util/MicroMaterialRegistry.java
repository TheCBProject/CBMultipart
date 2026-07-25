package codechicken.microblock.util;

import codechicken.microblock.api.MicroMaterial;
import net.covers1624.quack.util.CrashLock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NewRegistryEvent;

/**
 * Created by covers1624 on 26/6/22.
 */
public class MicroMaterialRegistry {

    private static final CrashLock LOCK = new CrashLock("Already initialized");

    public static void init(IEventBus modBus) {
        LOCK.lock();

        modBus.addListener(MicroMaterialRegistry::createRegistries);
    }

    private static void createRegistries(NewRegistryEvent event) {
        event.register(MicroMaterial.REGISTRY);
    }
}
