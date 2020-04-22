package codechicken.mixin.api;

import codechicken.lib.util.SneakyUtils;
import codechicken.mixin.util.Utils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static codechicken.lib.util.SneakyUtils.*;

/**
 * Provides an abstracted backend for the {@link MixinCompiler} system.
 * This allows different environments, to change the low level
 * integration {@link MixinCompiler} requires with the running environment.
 * I.e, Running under MinecraftForge requires _some_ tweaks.
 *
 * Created by covers1624 on 2/11/20.
 */
public interface MixinBackend {

    /**
     * Gets the bytes for a class.
     *
     * @param name The class name.
     * @return The bytes for the class.
     */
    @Nullable
    byte[] getBytes(@AsmName String name);

    /**
     * Defines a class.
     *
     * @param name  The name for the class.
     * @param bytes The bytes for the class.
     * @return The defined class.
     */
    @Nonnull
    Class<?> defineClass(String name, byte[] bytes);

    /**
     * Loads a class.
     *
     * @param name The class name.
     * @return The loaded class.
     */
    Class<?> loadClass(String name);

    /**
     * A simple {@link MixinBackend} implementation for standalone use.
     */
    class SimpleMixinBackend implements MixinBackend {

        protected static final Method m_defineClass;

        static {
            try {
                m_defineClass = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
                m_defineClass.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Unable to retrieve methods via reflection.", e);
            }
        }

        private final ClassLoader classLoader;

        public SimpleMixinBackend() {
            this(SimpleMixinBackend.class.getClassLoader());
        }

        public SimpleMixinBackend(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        @Override
        public byte[] getBytes(String name) {
            try (InputStream is = classLoader.getResourceAsStream(name + ".class")) {
                if (is == null) {
                    return null;
                }
                return IOUtils.toByteArray(is);
            } catch (IOException e) {
                throwUnchecked(new ClassNotFoundException("Could not load bytes for '" + name + "'.", e));
                return null;//never happens.
            }
        }

        @Override
        public Class<?> defineClass(String name, byte[] bytes) {
            try {
                return (Class<?>) m_defineClass.invoke(classLoader, bytes, 0, bytes.length);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to define class '" + name + "'.", e);
            }
        }

        @Override
        public Class<?> loadClass(String name) {
            try {
                return classLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }
}
