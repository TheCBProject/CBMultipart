package codechicken.microblock.util;

import codechicken.microblock.api.MicroMaterial;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Created by covers1624 on 26/6/22.
 */
public class MicroMaterialRegistry {

    private static final CrashLock LOCK = new CrashLock("Already initialized");

    @Deprecated
    public static Registry<MicroMaterial> microMaterials() {
        return MicroMaterial.REGISTRY;
    }

    public static void init(IEventBus modBus) {
        LOCK.lock();

        modBus.addListener(MicroMaterialRegistry::createRegistries);
    }

    private static void createRegistries(NewRegistryEvent event) {
        event.register(MicroMaterial.REGISTRY);
    }

    @Nullable
    public static MicroMaterial getMaterial(String name) {
        return getMaterial(ResourceLocation.parse(name));
    }

    @Nullable
    public static MicroMaterial getMaterial(ResourceLocation name) {
        return microMaterials().get(name);
    }

}
