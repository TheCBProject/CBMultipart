package codechicken.mixin.util;

import codechicken.mixin.api.MixinCompiler;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;

/**
 * Created by covers1624 on 2/11/20.
 */
public class ClassNodeInfo extends ClassInfo {

    private final ClassNode cNode;
    protected List<ClassInfo> interfaces;
    private final List<MethodInfo> methods;

    public ClassNodeInfo(MixinCompiler mixinCompiler, ClassNode cNode) {
        super(mixinCompiler);
        this.cNode = cNode;
        interfaces = cNode.interfaces.stream()//
                .map(mixinCompiler::getClassInfo)//
                .collect(Collectors.toList());
        methods = cNode.methods.stream()//
                .map(MethodNodeInfo::new)//
                .collect(Collectors.toList());
    }

    //@formatter:off
    @Override public String getName() { return cNode.name; }
    @Override public Optional<ClassInfo> getSuperClass() { return Optional.ofNullable(mixinCompiler.getClassInfo(cNode.superName)); }
    @Override public Stream<ClassInfo> getInterfaces() { return interfaces.stream(); }
    @Override public Stream<MethodInfo> getMethods() { return methods.stream(); }
    public ClassNode getCNode() { return cNode; }
    //@formatter:on

    public class MethodNodeInfo implements MethodInfo {

        private final MethodNode mNode;
        private final String[] exceptions;

        public MethodNodeInfo(MethodNode mNode) {
            this.mNode = mNode;
            exceptions = mNode.exceptions.toArray(new String[0]);
        }

        //@formatter:off
        @Override public ClassInfo getOwner() { return ClassNodeInfo.this; }
        @Override public String getName() { return mNode.name; }
        @Override public String getDesc() { return mNode.desc; }
        @Override public String[] getExceptions() { return exceptions; }
        @Override public boolean isPrivate() { return (mNode.access & ACC_PRIVATE) != 0; }
        @Override public boolean isAbstract() { return (mNode.access & ACC_ABSTRACT) != 0; }
        //@formatter:on
    }
}
