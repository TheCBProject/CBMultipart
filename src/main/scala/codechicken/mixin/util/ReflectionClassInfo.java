package codechicken.mixin.util;

import codechicken.mixin.api.MixinCompiler;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by covers1624 on 2/11/20.
 */
public class ReflectionClassInfo extends ClassInfo {

    private final Class<?> clazz;
    private final String name;
    private final List<ClassInfo> interfaces;
    private final List<MethodInfo> methods;

    public ReflectionClassInfo(MixinCompiler mixinCompiler, Class<?> clazz) {
        super(mixinCompiler);
        this.clazz = clazz;
        name = Utils.asmName(clazz.getName());
        interfaces = Arrays.stream(clazz.getInterfaces())//
                .map(mixinCompiler::getClassInfo)//
                .collect(Collectors.toList());
        methods = Arrays.stream(clazz.getMethods())//
                .map(ReflectionMethodInfo::new)//
                .collect(Collectors.toList());
    }

    //@formatter:off
    @Override public String getName() { return name; }
    @Override public Optional<ClassInfo> getSuperClass() { return Optional.ofNullable(mixinCompiler.getClassInfo(clazz.getSuperclass())); }
    @Override public Stream<ClassInfo> getInterfaces() { return interfaces.stream(); }
    @Override public Stream<MethodInfo> getMethods() { return methods.stream(); }
    //@formatter:on

    public class ReflectionMethodInfo implements MethodInfo {

        private final String name;
        private final String desc;
        private final String[] exceptions;
        private final boolean isPrivate;
        private final boolean isAbstract;

        private ReflectionMethodInfo(Method method) {
            name = method.getName();
            desc = Type.getType(method).getDescriptor();
            exceptions = Arrays.stream(method.getExceptionTypes())//
                    .map(Class::getName)//
                    .map(Utils::asmName)//
                    .toArray(String[]::new);
            isPrivate = Modifier.isPrivate(method.getModifiers());
            isAbstract = Modifier.isAbstract(method.getModifiers());
        }

        //@formatter:off
        @Override public ClassInfo getOwner() { return ReflectionClassInfo.this; }
        @Override public String getName() { return name; }
        @Override public String getDesc() { return desc; }
        @Override public String[] getExceptions() { return exceptions; }
        @Override public boolean isPrivate() { return isPrivate; }
        @Override public boolean isAbstract() { return isAbstract; }
        //@formatter:on
    }

}
