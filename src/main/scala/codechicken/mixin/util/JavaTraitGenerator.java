package codechicken.mixin.util;

import codechicken.asm.ASMHelper;
import codechicken.asm.InsnComparator;
import codechicken.asm.InsnListSection;
import codechicken.mixin.api.MixinCompiler;
import codechicken.mixin.scala.StackAnalyser;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;

/**
 * Handles taking a java class, and turning it into a Scala-like trait interface.
 *
 * Created by covers1624 on 2/11/20.
 */
public class JavaTraitGenerator {

    protected final MixinCompiler mixinCompiler;
    protected final ClassNode cNode;
    protected final ClassNode tNode;

    protected List<FieldMixin> fields;
    protected Map<String, String> fieldNameLookup;
    protected List<String> supers = new ArrayList<>();
    protected List<MethodNode> methods = new ArrayList<>();
    protected Set<String> methodSigs;
    protected MixinInfo mixinInfo;

    public JavaTraitGenerator(MixinCompiler mixinCompiler, ClassNode cNode) {
        this.mixinCompiler = mixinCompiler;
        this.cNode = cNode;
        this.tNode = new ClassNode();
        checkNode();
        operate();
    }

    protected void checkNode() {
        preCheckNode();
        if ((cNode.access & ACC_INTERFACE) != 0) {
            throw new IllegalArgumentException("Cannot register java interface '" + cNode.name + "' as a mixin trait.");
        }
        if (!cNode.innerClasses.isEmpty() && cNode.innerClasses.stream().noneMatch(this::checkInner)) {
            throw new IllegalArgumentException("Found illegal inner class for '" + cNode.name + "', use scala.");
        }

        if ((cNode.access & ACC_ABSTRACT) != 0) {
            throw new IllegalArgumentException("Cannot register abstract class " + cNode.name + " as a java mixin trait. Use scala");
        }
    }

    protected void operate() {
        preProcessTrait();
        fields = cNode.fields.stream()//
                .map(f -> new FieldMixin(f.name, f.desc, f.access))//
                .collect(Collectors.toList());
        fieldNameLookup = fields.stream().collect(Collectors.toMap(FieldMixin::getName, e -> e.getAccessName(cNode.name)));
        methodSigs = cNode.methods.stream().map(e -> e.name + e.desc).collect(Collectors.toSet());

        beforeTransform();

        tNode.visit(V1_8, ACC_INTERFACE | ACC_ABSTRACT | ACC_PUBLIC, cNode.name, null, "java/lang/Object", cNode.interfaces.toArray(new String[0]));
        tNode.sourceFile = cNode.sourceFile;

        fields.forEach(f -> {
            tNode.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, fieldNameLookup.get(f.getName()), "()" + f.getDesc(), null, null);
            tNode.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, fieldNameLookup.get(f.getName()) + "_$eq", "(" + f.getDesc() + ")V", null, null);
        });
        cNode.methods.forEach(this::convertMethod);
        postProcessTrait();
        mixinInfo = new MixinInfo(tNode.name, cNode.superName, Collections.emptyList(), fields, methods, supers);
    }

    protected void preCheckNode() {
    }

    protected void preProcessTrait() {
    }

    protected void beforeTransform() {
    }

    protected void postProcessTrait() {
    }

    public ClassNode getClassNode() {
        return tNode;
    }

    public MixinInfo getMixinInfo() {
        return mixinInfo;
    }

    private void staticTransform(MethodNode mNode, MethodNode base) {
        StackAnalyser stack = new StackAnalyser(Type.getObjectType(cNode.name), base);
        InsnList insnList = mNode.instructions;
        InsnPointer pointer = new InsnPointer(insnList);

        AbstractInsnNode insn;
        while ((insn = pointer.get()) != null) {
            if (insn instanceof FieldInsnNode) {
                FieldInsnNode fInsn = (FieldInsnNode) insn;
                if (insn.getOpcode() == GETFIELD) {
                    pointer.replace(new MethodInsnNode(INVOKEINTERFACE, cNode.name, fieldNameLookup.get(fInsn.name), "()" + fInsn.desc, true));
                } else if (insn.getOpcode() == PUTFIELD) {
                    pointer.replace(new MethodInsnNode(INVOKEINTERFACE, cNode.name, fieldNameLookup.get(fInsn.name) + "_$eq", "(" + fInsn.desc + ")V", true));
                }
            } else if (insn instanceof MethodInsnNode) {
                MethodInsnNode mInsn = (MethodInsnNode) insn;
                if (mInsn.getOpcode() == INVOKESPECIAL) {
                    getSuper(mInsn, stack).ifPresent(e -> {
                        String bridgeName = cNode.name.replace("/", "$") + "$$super$" + mInsn.name;
                        if (supers.add(mInsn.name + mInsn.desc)) {
                            tNode.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, bridgeName, mInsn.desc, null, null);
                        }
                        pointer.replace(new MethodInsnNode(INVOKEINTERFACE, cNode.name, bridgeName, mInsn.desc, true));
                    });
                } else if (mInsn.getOpcode() == INVOKEVIRTUAL) {
                    if (mInsn.owner.equals(cNode.name)) {
                        if (methodSigs.contains(mInsn.name + mInsn.desc)) {
                            //call the interface method
                            pointer.replace(new MethodInsnNode(INVOKEINTERFACE, mInsn.owner, mInsn.name, mInsn.desc, true));
                        } else {
                            //cast to parent class and call
                            Type mType = Type.getMethodType(mInsn.desc);
                            StackAnalyser.StackEntry instanceEntry = stack.peek(StackAnalyser.width(mType.getArgumentTypes()));
                            insnList.insert(instanceEntry.insn(), new TypeInsnNode(CHECKCAST, cNode.superName));
                            mInsn.owner = cNode.superName;
                        }
                    }
                }
            }

            stack.visitInsn(pointer.get());
            pointer.advance();
        }
    }

    private void convertMethod(MethodNode mNode) {
        if (mNode.name.equals("<clinit>")) {
            throw new IllegalArgumentException("Static initializers are not permitted " + mNode.name + " as a mixin trait");
        }
        if (mNode.name.equals("<init>")) {
            if (!mNode.desc.equals("()V")) {
                throw new IllegalArgumentException("Constructor arguments are not permitted " + mNode.name + " as a mixin trait");
            }
            MethodNode mv = staticClone(mNode, "$init$", ACC_PUBLIC);

            //Strip super constructor call.
            InsnListSection insns = new InsnListSection();
            insns.add(new VarInsnNode(ALOAD, 0));
            insns.add(new MethodInsnNode(INVOKESPECIAL, cNode.superName, "<init>", "()V", false));

            InsnListSection mInsns = new InsnListSection(mv.instructions);
            InsnListSection found = InsnComparator.matches(mInsns, insns, Collections.emptySet());
            if (found == null) {
                throw new IllegalArgumentException("Invalid constructor insn sequence " + cNode.name + "\n" + mInsns);
            }
            found.trim(Collections.emptySet()).remove();
            staticTransform(mv, mNode);
            return;
        }
        if ((mNode.access & ACC_PRIVATE) == 0) {
            MethodVisitor mv = tNode.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, mNode.name, mNode.desc, null, mNode.exceptions.toArray(new String[0]));
            methods.add((MethodNode) mv);
        }
        int access = (mNode.access & ACC_PRIVATE) == 0 ? ACC_PUBLIC : ACC_PRIVATE;
        MethodNode mv = staticClone(mNode, mNode.name + "$", access);
        staticTransform(mv, mNode);
    }

    private Optional<MethodInfo> getSuper(MethodInsnNode mInsn, StackAnalyser stack) {
        if (mInsn.owner.equals(stack.owner().getInternalName())) {
            return Optional.empty();//not a super call
        }

        //super calls are either to methods with the same name or contain a pattern 'target$$super$name' from the scala compiler
        String methodName = stack.m().name.replaceAll(".+\\Q$$super$\\E", "");
        if (!mInsn.name.equals(methodName)) {
            return Optional.empty();
        }

        StackAnalyser.StackEntry entry = stack.peek(Type.getType(mInsn.desc).getArgumentTypes().length);
        if (!(entry instanceof StackAnalyser.Load)) {
            return Optional.empty();
        }
        StackAnalyser.Load load = (StackAnalyser.Load) entry;
        if (!(load.e() instanceof StackAnalyser.This)) {
            return Optional.empty();
        }

        return mixinCompiler.getClassInfo(stack.owner().getInternalName()).getSuperClass()//
                .flatMap(e -> e.findPublicImpl(methodName, mInsn.desc));
    }

    private MethodNode staticClone(MethodNode mNode, String name, int access) {
        MethodNode mv = (MethodNode) tNode.visitMethod(access | ACC_STATIC, name, Utils.staticDesc(cNode.name, mNode.desc), null, mNode.exceptions.toArray(new String[0]));
        ASMHelper.copy(mNode, mv);
        return mv;
    }

    //true if pass
    private boolean checkInner(InnerClassNode innerNode) {
        if (innerNode.outerName == null) {
            return false;
        }
        if (cNode.name.equals(innerNode.outerName)) {
            return false;
        }
        if (innerNode.name.startsWith(cNode.name)) {
            return false;
        }
        return true;
    }

    public static class InsnPointer {

        public final InsnList insnList;
        public AbstractInsnNode pointer;

        public InsnPointer(InsnList insnList) {
            this.insnList = insnList;
            pointer = insnList.getFirst();
        }

        private void replace(AbstractInsnNode newInsn) {
            insnList.insert(pointer, newInsn);
            insnList.remove(pointer);
            pointer = newInsn;
        }

        public AbstractInsnNode get() {
            return pointer;
        }

        public AbstractInsnNode advance() {
            return pointer = pointer.getNext();
        }
    }

}
