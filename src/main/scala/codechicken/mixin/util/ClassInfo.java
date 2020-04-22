package codechicken.mixin.util;

import codechicken.mixin.api.MixinCompiler;
import com.google.common.collect.Streams;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by covers1624 on 2/11/20.
 */
@SuppressWarnings ("UnstableApiUsage")
public abstract class ClassInfo {

    protected MixinCompiler mixinCompiler;

    protected ClassInfo(MixinCompiler mixinCompiler) {
        this.mixinCompiler = mixinCompiler;
    }

    public abstract String getName();

    public abstract Optional<ClassInfo> getSuperClass();

    public abstract Stream<ClassInfo> getInterfaces();

    public abstract Stream<MethodInfo> getMethods();

    public Stream<MethodInfo> getParentMethods() {
        return Streams.concat(//
                Streams.stream(getSuperClass()),//
                getInterfaces()//
        ).flatMap(ClassInfo::getAllMethods);
    }

    public Stream<MethodInfo> getAllMethods() {
        return Streams.concat(getMethods(), getParentMethods());
    }

    public Optional<MethodInfo> findPublicImpl(String name, String desc) {
        return getAllMethods()//
                .filter(m -> m.getName().equals(name))//
                .filter(m -> m.getDesc().equals(desc))//
                .filter(m -> !m.isAbstract() && !m.isPrivate())//
                .findFirst();
    }

    public Optional<ClassInfo> concreteParent() {
        return getSuperClass();
    }

    public boolean inheritsFrom(String parentName) {
        return Streams.concat(Streams.stream(concreteParent()), getInterfaces()).parallel()//
                .anyMatch(e -> e.getName().equals(parentName) || e.inheritsFrom(parentName));
    }

    public String getModuleName() {
        return getName();
    }

}
