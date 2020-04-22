package codechicken.mixin.util;

import codechicken.mixin.scala.StackAnalyser;
import com.google.common.collect.Streams;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by covers1624 on 2/11/20.
 */
public class Utils {

    public static String asmName(Class<?> clazz) {
        return asmName(clazz.getName());
    }

    public static String asmName(String name) {
        return name.replace(".", "/");
    }

    /**
     * Represents this Enumeration as an Iterable.
     *
     * @param enumeration The Enumeration.
     * @param <E>         The Type.
     * @return The Iterable.
     */
    public static <E> Iterable<E> toIterable(Enumeration<E> enumeration) {
        return () -> new Iterator<E>() {
            //@formatter:off
            @Override public boolean hasNext() { return enumeration.hasMoreElements(); }
            @Override public E next() { return enumeration.nextElement(); }
            //@formatter:on
        };
    }

    public static <T> Optional<Constructor<T>> findConstructor(Class<T> clazz, Class<?>... parameters) {
        try {
            return Optional.of(clazz.getConstructor(parameters));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    public static <T> T newInstance(Constructor<T> ctor, Object... args) {
        try {
            return ctor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to instantiate class.", e);
        }
    }

    public static <T> Stream<T> of(T thing) {
        return thing != null ? Stream.of(thing) : Stream.empty();
    }

    public static <T> List<T> of(T first, List<T> rest) {
        if (first == null) {
            return rest;
        }
        List<T> list = new ArrayList<>(rest.size() + 1);
        list.add(first);
        list.addAll(rest);
        return list;
    }

    public static String staticDesc(String owner, String desc) {
        Type descT = Type.getMethodType(desc);
        List<Type> args = new ArrayList<>(Arrays.asList(descT.getArgumentTypes()));
        args.add(0, Type.getType("L" + owner + ";"));
        return Type.getMethodDescriptor(descT.getReturnType(), args.toArray(new Type[0]));
    }

    public static String timeString(long start, long end) {
        long delta = end - start;
        long millis = TimeUnit.NANOSECONDS.toMillis(delta);

        String s;
        if (millis >= 5) {
            s = millis + "ms(" + delta + "ns)";
        } else {
            s = delta + "ns";
        }
        return s;

    }

    @SuppressWarnings ("UnstableApiUsage")
    public static Stream<ClassInfo> allParents(ClassInfo info) {
        return Streams.concat(//
                of(info),//
                Streams.concat(//
                        Streams.stream(info.getSuperClass()),//
                        info.getInterfaces()//
                ).flatMap(Utils::allParents)//
        );
    }

    public static void finishBridgeCall(MethodVisitor mv, String mvDesc, int opcode, String owner, String name, String desc) {
        Type[] args = Type.getArgumentTypes(mvDesc);
        Type returnType = Type.getReturnType(mvDesc);
        int localIndex = 1;
        for (Type arg : args) {
            mv.visitVarInsn(arg.getOpcode(ILOAD), localIndex);
            localIndex += StackAnalyser.width(arg);
        }
        mv.visitMethodInsn(opcode, owner, name, desc, opcode == INVOKEINTERFACE);
        mv.visitInsn(returnType.getOpcode(IRETURN));
        mv.visitMaxs(-1, -1);//COMPUTE_FRAMES :)
    }

    public static void writeBridge(MethodVisitor mv, String mvDesc, int opcode, String owner, String name, String desc) {
        mv.visitVarInsn(ALOAD, 0);
        finishBridgeCall(mv, mvDesc, opcode, owner, name, desc);
    }

    public static void writeStaticBridge(MethodNode mv, String mName, MixinInfo info) {
        writeBridge(mv, mv.desc, INVOKESTATIC, info.getName(), mName + "$", staticDesc(info.getName(), mv.desc));
    }
}
