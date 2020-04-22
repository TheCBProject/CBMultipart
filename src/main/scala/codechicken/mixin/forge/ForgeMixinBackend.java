package codechicken.mixin.forge;

import codechicken.mixin.api.MixinBackend;
import cpw.mods.modlauncher.TransformingClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by covers1624 on 2/11/20.
 */
public class ForgeMixinBackend extends MixinBackend.SimpleMixinBackend {

    private static final Method m_buildTransformedClassNodeFor;

    static {
        try {
            m_buildTransformedClassNodeFor = TransformingClassLoader.class.getDeclaredMethod("buildTransformedClassNodeFor", String.class, String.class);
            m_buildTransformedClassNodeFor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to retrieve methods via reflection.", e);
        }
    }

    private final TransformingClassLoader classLoader;

    public ForgeMixinBackend() {
        classLoader = (TransformingClassLoader) getClass().getClassLoader();
    }

    @Override
    public byte[] getBytes(String name) {
        String jName = name.replace("/", ".");
        if (jName.equals("java.lang.Object")) {
            return null;
        }
        try {
            return (byte[]) m_buildTransformedClassNodeFor.invoke(classLoader, jName, "codechicken.mixin.forge.ForgeMixinBackend");
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to get bytes for class '" + name + "'.", e);
        }
    }
}
