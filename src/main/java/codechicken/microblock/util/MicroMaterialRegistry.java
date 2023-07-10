package codechicken.microblock.util;

import codechicken.microblock.api.MicroMaterial;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.jetbrains.annotations.Nullable;

import static codechicken.microblock.CBMicroblock.MOD_ID;

/**
 * Created by covers1624 on 26/6/22.
 */
public class MicroMaterialRegistry {

    private static final CrashLock LOCK = new CrashLock("Already initialized");

    public static IForgeRegistry<MicroMaterial> MICRO_MATERIALS;

    public static void init() {
        LOCK.lock();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(MicroMaterialRegistry::createRegistries);
    }

    private static void createRegistries(NewRegistryEvent event) {
        event.create(new RegistryBuilder<MicroMaterial>()
                        .setName(new ResourceLocation(MOD_ID, "micro_material"))
                        .disableSaving()
                        .allowModification(),
                e -> MICRO_MATERIALS = e
        );
    }

    @Nullable
    public static MicroMaterial getMaterial(String name) {
        return getMaterial(new ResourceLocation(name));
    }

    @Nullable
    public static MicroMaterial getMaterial(ResourceLocation name) {
        return MICRO_MATERIALS.getValue(name);
    }

}
