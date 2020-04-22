package codechicken.mixin.api;

import codechicken.asm.ASMHelper;
import codechicken.mixin.util.JavaTraitGenerator;
import codechicken.mixin.scala.MixinScalaLanguageSupport;
import codechicken.mixin.scala.ScalaClassInfo;
import codechicken.mixin.util.ClassInfo;
import codechicken.mixin.util.ClassNodeInfo;
import codechicken.mixin.util.MixinInfo;
import codechicken.mixin.util.ReflectionClassInfo;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.BiFunction;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

/**
 * Defines abstract logic for loading Mixins for different languages.
 * These are loaded using a {@link ServiceLoader} from the classpath.
 *
 * Created by covers1624 on 2/16/20.
 */
public interface MixinLanguageSupport {

    /**
     * Tries to load a {@link ClassInfo} for the given {@link ClassNode}.
     * Custom implementations shouldn't be greedy, only load a
     * {@link ClassInfo} if you know for certain that you need to.
     * I.e: {@link MixinScalaLanguageSupport}, only loads a {@link ScalaClassInfo} if
     * the class has a ScalaSignature Annotation, and is a Scala trait class.
     * The only exception to this is the default Java implementation of this method
     * it will greedy load ClassInfo's for everything.
     *
     * @param name  The name of the class.
     * @param cNode The ClassNode.
     * @return The ClassInfo.
     */
    Optional<ClassInfo> obtainInfo(String name, @Nullable ClassNode cNode);

    /**
     * Tries to build a {@link MixinInfo} for the given {@link ClassNode}.
     * as with {@link #obtainInfo}, only load MixinInfos if you
     * know for certain that you need to. I.e: {@link MixinScalaLanguageSupport},
     * will only load a MixinInfo for the class, if {@link MixinCompiler#getClassInfo(ClassNode)}
     * resolves to a {@link ScalaClassInfo}.
     *
     * @param cNode The ClassNode.
     * @return The MixinInfo.
     */
    Optional<MixinInfo> buildMixinTrait(ClassNode cNode);

    /**
     * The name for the MixinLanguageSupport, Required.
     * Must be unique.
     */
    @Target (ElementType.TYPE)
    @Retention (RetentionPolicy.RUNTIME)
    @interface LanguageName {

        String value();
    }

    /**
     * A simple way to sort this {@link MixinLanguageSupport} before others in the list.
     * A smaller number will be earlier in the list. E.g: [-3000, -100, 0, 100, 3000]
     * If this annotation is not provided, it will default to an index of 1000.
     */
    @Target (ElementType.TYPE)
    @Retention (RetentionPolicy.RUNTIME)
    @interface SortingIndex {

        int value();
    }

    /**
     * The default java handling for MixinCompiler.
     */
    @LanguageName ("java")
    @SortingIndex (Integer.MAX_VALUE)
    class JavaMixinLanguageSupport implements MixinLanguageSupport {

        protected final MixinCompiler mixinCompiler;
        private BiFunction<MixinCompiler, ClassNode, JavaTraitGenerator> traitGeneratorFactory = JavaTraitGenerator::new;

        public JavaMixinLanguageSupport(MixinCompiler mixinCompiler) {
            this.mixinCompiler = mixinCompiler;
        }

        public void setTraitGeneratorFactory(BiFunction<MixinCompiler, ClassNode, JavaTraitGenerator> factory) {
            this.traitGeneratorFactory = factory;
        }

        @Override
        public Optional<ClassInfo> obtainInfo(String name, ClassNode cNode) {
            if (cNode != null) {
                return Optional.of(new ClassNodeInfo(mixinCompiler, cNode));
            }
            Class<?> clazz = mixinCompiler.getMixinBackend().loadClass(name.replace("/", "."));
            if (clazz != null) {
                return Optional.of(new ReflectionClassInfo(mixinCompiler, clazz));
            }
            return Optional.empty();
        }

        @Override
        public Optional<MixinInfo> buildMixinTrait(ClassNode cNode) {
            JavaTraitGenerator generator = traitGeneratorFactory.apply(mixinCompiler, cNode);
            ClassNode tNode = generator.getClassNode();
            MixinInfo info = generator.getMixinInfo();
            mixinCompiler.defineClass(tNode.name, ASMHelper.createBytes(tNode, COMPUTE_FRAMES | COMPUTE_MAXS));
            return Optional.of(info);
        }
    }

}
