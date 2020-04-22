package codechicken.mixin.api;

import codechicken.mixin.MixinCompilerImpl;
import codechicken.mixin.util.ClassInfo;
import codechicken.mixin.util.MixinInfo;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

/**
 * Defines a compiler capable of generating a composite Class, comprised of
 * a Set of Scala-like Trait classes on top of a base Class implementation.
 *
 * Created by covers1624 on 2/17/20.
 */
public interface MixinCompiler {

    /**
     * Create a {@link MixinCompiler} instance.
     *
     * @return The instance.
     */
    static MixinCompiler create() {
        return new MixinCompilerImpl();
    }

    /**
     * Create a {@link MixinCompiler} instance, with the given MixinBackend.1
     *
     * @param backend The MixinBackend.
     * @return The instance.
     */
    static MixinCompiler create(MixinBackend backend) {
        return new MixinCompilerImpl(backend);
    }

    /**
     * Gets the {@link MixinBackend} for this MixinCompiler
     *
     * @return The MixinBackend instance.
     */
    MixinBackend getMixinBackend();

    /**
     * Attempts to find a {@link MixinLanguageSupport} with the given name.
     *
     * @param name The name.
     * @return The MixinLanguageSupport instance.
     */
    <T extends MixinLanguageSupport> Optional<T> findLanguageSupport(String name);

    /**
     * Gets a {@link ClassInfo} instance for the given class name.
     *
     * @param name The class name.
     * @return The ClassInfo
     */
    @Nullable
    ClassInfo getClassInfo(@Nullable @AsmName String name);

    /**
     * Overload for {@link #getClassInfo(String)}, taking a {@link ClassNode} instead.
     *
     * @param node The ClassNode.
     * @return The ClassInfo.
     */
    @Nullable
    default ClassInfo getClassInfo(@Nonnull ClassNode node) {
        return getClassInfo(node.name);
    }

    /**
     * Overload for {@link #getClassInfo(String)}, taking a {@link Class} instead.
     *
     * @param clazz The Class.
     * @return The ClassInfo.
     */
    @Nullable
    default ClassInfo getClassInfo(@Nullable Class<?> clazz) {
        return clazz == null ? null : getClassInfo(clazz.getName().replace(".", "/"));
    }

    /**
     * Loads a {@link ClassNode} for the given class name.
     *
     * @param name The Class name.
     * @return The ClassNode.
     */
    @Nullable
    ClassNode getClassNode(@AsmName @Nonnull String name);

    /**
     * Registers a Trait to the {@link MixinCompiler}.
     *
     * @param cNode The ClassNode for the trait.
     * @return The MixinInfo for the trait.
     */
    MixinInfo registerTrait(ClassNode cNode);

    /**
     * Gets a {@link MixinInfo} for the given Class name.
     *
     * @param name The Class name
     * @return The MixinInfo, Null if it does not exist.
     */
    @Nullable
    MixinInfo getMixinInfo(@AsmName String name);

    /**
     * Defines a class.
     *
     * @param name  The name for the class.
     * @param bytes The bytes for the class.
     * @return The defined class.
     */
    @Nonnull
    Class<?> defineClass(@AsmName String name, byte[] bytes);

    /**
     * Compiles a new class with the given name, super Class, and traits.
     *
     * @param name       The name for the class.
     * @param superClass The name for the super class.s
     * @param traits     The Traits to mixin.
     * @return The compiled class.
     */
    <T> Class<T> compileMixinClass(String name, String superClass, Set<String> traits);

}
