package codechicken.multipart.util;

import codechicken.asm.ClassHierarchyManager;
import codechicken.mixin.api.MixinBackend;
import net.covers1624.quack.reflect.PrivateLookups;
import net.neoforged.fml.classloading.transformation.TransformingClassLoader;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * Created by covers1624 on 2/11/20.
 */
public class ForgeMixinBackend extends MixinBackend.SimpleMixinBackend {

    private static final TransformingClassLoader cl = (TransformingClassLoader) Thread.currentThread().getContextClassLoader();
    private static final MethodHandle m_buildTransformedClassNodeFor;

    static {
        try {
            m_buildTransformedClassNodeFor = PrivateLookups.getTrustedLookup()
                    .findVirtual(TransformingClassLoader.class, "getMaybeTransformedClassBytes", MethodType.methodType(byte[].class, String.class, String.class));
        } catch (Throwable e) {
            throw new RuntimeException("Unable to retrieve methods via reflection.", e);
        }
        ClassHierarchyManager.addByteLookupFunc(cName -> getBytesForClass(cName, "neoforge:computing_frames"));
    }

    public ForgeMixinBackend() {
        super(cl);
    }

    @Override
    public byte @Nullable [] getBytes(String name) {
        return getBytesForClass(name, "cbmultipart:mixin");
    }

    @Override
    public boolean filterMethodAnnotations(String annType, String value) {
        if (FMLEnvironment.getDist() == null) {
            return false;
        }
        String side = "net.minecraftforge.api.distmarker.Dist." + FMLEnvironment.getDist().name();
        return annType.equals("net.minecraftforge.api.distmarker.OnlyIn") && !value.equals(side);
    }

    private static byte @Nullable [] getBytesForClass(String cName, String reason) {
        String jName = cName.replace("/", ".");
        if (jName.equals("java.lang.Object")) return null;

        try {
            return (byte[]) m_buildTransformedClassNodeFor.invokeExact(cl, jName, reason);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to get bytes for class '" + cName + "'.", e);
        }
    }
}
