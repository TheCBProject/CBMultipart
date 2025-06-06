package codechicken.multipart.wrapped.level;

import codechicken.asm.CC_ClassWriter;
import codechicken.multipart.util.MultipartGenerator;
import com.google.common.base.Suppliers;
import net.covers1624.quack.asm.ClassBuilder;
import net.covers1624.quack.collection.FastStream;
import net.covers1624.quack.reflect.PrivateLookups;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * Generates Level classes extending either {@link ServerLevel} or {@link ClientLevel}, implementing {@link WrapperLevel}.
 * <p>
 * These classes have a few limitations.
 * - Only public non-final api surface is proxied to the wrapped world.
 * - Copy constructors are implemented via reflection for object creation.
 * <p>
 * Created by covers1624 on 6/1/25.
 */
// TODO, We can improve these clases by:
//       - Implementing the copy constructors with asm directly on ServerLevel/ClientLevel/Level/AttachmentHolder.
//       - De-finaling all (or selected) methods with asm on the level classes, so we can proxy them.
//       The latter is the most important imo.
public abstract class WrapperLevelFactory {

    protected static final Unsafe UNSAFE;
    protected static final MethodHandles.Lookup LOOKUP = PrivateLookups.getTrustedLookup();

    static {
        try {
            var theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (Throwable ex) {
            throw new RuntimeException("Failed to get Unsafe.", ex);
        }
    }

    private static final Supplier<WrapperLevelFactory> CLIENT = Suppliers.memoize(() -> buildSidedLevelWrapper(Dist.CLIENT));
    private static final Supplier<WrapperLevelFactory> SERVER = Suppliers.memoize(() -> buildSidedLevelWrapper(Dist.DEDICATED_SERVER));

    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    public static void init() {
        LOCK.lock();
        CLIENT.get();
        SERVER.get();
    }

    public static Level makeLevel(Level wrapped, WrapperBlockProvider provider) {
        // TODO this is not server safe.
        if (wrapped instanceof ClientLevel) {
            return CLIENT.get().instantiate(wrapped, provider);
        }
        if (wrapped instanceof ServerLevel) {
            return SERVER.get().instantiate(wrapped, provider);
        }
        throw new RuntimeException("Unable to make wrapper for world class: " + wrapped.getClass().getName());
    }

    private static WrapperLevelFactory buildSidedLevelWrapper(Dist side) {
        ClassNode templateClass = MultipartGenerator.MIXIN_COMPILER.getClassNode(getType(WrapperLevelTemplate.class).getInternalName());
        if (templateClass == null) throw new RuntimeException("Failed to find/parse template class.");

        Type c_level = getType(Level.class);
        // TODO this is not server safe.
        Class<?> sideClazz = switch (side) {
            case CLIENT -> ClientLevel.class;
            case DEDICATED_SERVER -> ServerLevel.class;
        };
        ClassBuilder cb = new ClassBuilder(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES, sideName("codechicken/multipart/$$WrapperLevel", side))
                .withClassVersion(templateClass.version)
                .withParent(getType(sideClazz))
                .withInterface(getType(WrapperLevel.class));

        var wrappedLevelField = cb.addField(ACC_PRIVATE | ACC_FINAL, "wrapped", getType(sideClazz));
        var providerField = cb.addField(ACC_PRIVATE | ACC_FINAL, "provider", getType(WrapperBlockProvider.class));

        var wrappedMethod = cb.addMethod(ACC_PUBLIC | ACC_FINAL, "wrapped", getMethodType(getType(sideClazz)))
                .withBody(gen -> {
                    gen.loadThis();
                    gen.getField(wrappedLevelField);
                    gen.ret();
                });

        // Return type overriding requires bridge method.
        cb.addMethod(ACC_PUBLIC | ACC_FINAL | ACC_BRIDGE, "wrapped", getMethodType(c_level))
                .withBody(gen -> {
                    gen.loadThis();
                    gen.methodInsn(INVOKEVIRTUAL, wrappedMethod);
                    gen.ret();
                });

        cb.addMethod(ACC_PUBLIC | ACC_FINAL, "provider", getMethodType(getType(WrapperBlockProvider.class)))
                .withBody(gen -> {
                    gen.loadThis();
                    gen.getField(providerField);
                    gen.ret();
                });

        TargetCollector collector = new TargetCollector();
        collector.visitHierarchy(sideClazz);

        cb.addMethod(ACC_PUBLIC | ACC_FINAL, "pullContext", getMethodType(Type.VOID_TYPE))
                .withBody(gen -> {
                    for (Field field : collector.copyAtRuntime) {
                        gen.loadThis();
                        gen.loadThis();
                        gen.getField(wrappedLevelField);
                        gen.fieldInsn(GETFIELD, getType(sideClazz), field.getName(), getType(field.getType()));
                        gen.fieldInsn(PUTFIELD, cb.name(), field.getName(), getType(field.getType()));
                    }
                    gen.ret();
                });

        cb.addMethod(ACC_PUBLIC | ACC_FINAL, "pushContext", getMethodType(Type.VOID_TYPE))
                .withBody(gen -> {
                    for (Field field : collector.copyAtRuntime) {
                        gen.loadThis();
                        gen.getField(wrappedLevelField);
                        gen.loadThis();
                        gen.fieldInsn(GETFIELD, cb.name(), field.getName(), getType(field.getType()));
                        gen.fieldInsn(PUTFIELD, getType(sideClazz), field.getName(), getType(field.getType()));
                    }
                    gen.ret();
                });

        // Clone methods over from the template.
        Set<MethodTarget> implemented = new HashSet<>();
        for (MethodNode method : templateClass.methods) {
            if (method.name.equals("<init>")) continue;

            var desc = getMethodType(method.desc);
            implemented.add(new MethodTarget(method.name, desc));

            cb.addMethod(method.access, method.name, desc)
                    .withBodyRaw(mv -> method.accept(new MethodVisitor(ASM9, mv) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                            // Remap any references to the template, to the generated class.
                            if (owner.equals(templateClass.name)) {
                                owner = cb.name().getInternalName();
                            }
                            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                        }
                    }));
        }

        for (MethodTarget method : collector.bridgedMethods) {
            // Don't make bridges for templated methods.
            if (!implemented.add(method)) continue;

            cb.addMethod(ACC_PUBLIC | ACC_FINAL, method.name, method.desc)
                    .withBody(gen -> {
                        gen.loadThis();
                        gen.getField(wrappedLevelField);
                        for (int i = 0; i < gen.numParams(); i++) {
                            gen.loadParam(i);
                        }
                        gen.methodInsn(INVOKEVIRTUAL, cb.parent(), method.name, method.desc, false);
                        gen.ret();
                    });
        }

        var clazz = define(cb);
        try {
            MethodHandle wrappedSetter = LOOKUP.findSetter(clazz, "wrapped", sideClazz);
            MethodHandle providerSetter = LOOKUP.findSetter(clazz, "provider", WrapperBlockProvider.class);

            List<FieldPair> fieldPairs = new ArrayList<>();
            for (Field field : collector.copyOnConstruction) {
                fieldPairs.add(new FieldPair(
                        field,
                        LOOKUP.findGetter(sideClazz, field.getName(), field.getType()),
                        LOOKUP.findSetter(clazz, field.getName(), field.getType())
                ));
            }

//            return createFallbackFactory(clazz, wrappedSetter, providerSetter, fieldPairs);
            return createAsmFactory(clazz, sideClazz, wrappedSetter, providerSetter, fieldPairs);
        } catch (Throwable ex) {
            throw new RuntimeException("Failed to create factory instance.", ex);
        }
    }

    public abstract Level instantiate(Level wrapped, WrapperBlockProvider provider);

    private static WrapperLevelFactory createAsmFactory(Class<?> clazz, Class<?> levelType, MethodHandle wrappedSetter, MethodHandle providerSetter, List<FieldPair> fields) {
        Type c_object = getType(Object.class);
        Type c_unsafe = getType(Unsafe.class);
        Type c_class = getType(Class.class);
        Type c_level = getType(Level.class);
        Type c_ourLevel = getType(clazz);
        Type c_sidedLevel = getType(levelType);
        Type c_methodHandle = getType(MethodHandle.class);
        Type c_provider = getType(WrapperBlockProvider.class);
        Type c_methodHandleArray = getType(MethodHandle[].class);
        Type c_wrappedLevelFactory = getType(WrapperLevelFactory.class);
        ClassBuilder cb = new ClassBuilder(ACC_PUBLIC | ACC_FINAL | ACC_SUPER, Type.getObjectType(c_ourLevel.getInternalName() + "$Factory"))
                .withClassVersion(V21)
                .withParent(c_wrappedLevelFactory);

        var clazzField = cb.addField(ACC_PRIVATE | ACC_FINAL, "clazz", c_class);
        var wrappedSetterField = cb.addField(ACC_PRIVATE | ACC_FINAL, "wrappedSetter", c_methodHandle);
        var providerSetterField = cb.addField(ACC_PRIVATE | ACC_FINAL, "providerSetter", c_methodHandle);

        Map<MethodHandle, ClassBuilder.FieldBuilder> fieldBuilders = new IdentityHashMap<>();

        for (FieldPair field : fields) {
            var getterField = cb.addField(ACC_PRIVATE | ACC_FINAL, field.field().getName() + "_getter", c_methodHandle);
            fieldBuilders.put(field.getter, getterField);

            var setterField = cb.addField(ACC_PRIVATE | ACC_FINAL, field.field().getName() + "_setter", c_methodHandle);
            fieldBuilders.put(field.setter, setterField);
        }

        cb.addMethod(ACC_PUBLIC, "<init>", getMethodType(VOID_TYPE, c_class, c_methodHandle, c_methodHandle, c_methodHandleArray))
                .withBody(gen -> {
                    gen.loadThis();
                    gen.methodInsn(INVOKESPECIAL, c_wrappedLevelFactory, "<init>", getMethodType(VOID_TYPE), false);

                    // assign easy ctor args.
                    gen.loadThis();
                    gen.loadParam(0);
                    gen.putField(clazzField);

                    gen.loadThis();
                    gen.loadParam(1);
                    gen.putField(wrappedSetterField);

                    gen.loadThis();
                    gen.loadParam(2);
                    gen.putField(providerSetterField);

                    // Parsed array of interleaved getter/setter for each field.
                    int i = 0;
                    for (FieldPair field : fields) {
                        gen.loadThis();
                        gen.loadParam(3);
                        gen.ldcInt(i++);
                        gen.insn(AALOAD);
                        gen.putField(fieldBuilders.get(field.getter));

                        gen.loadThis();
                        gen.loadParam(3);
                        gen.ldcInt(i++);
                        gen.insn(AALOAD);
                        gen.putField(fieldBuilders.get(field.setter));
                    }

                    gen.ret();
                });

        cb.addMethod(ACC_PUBLIC | ACC_FINAL, "instantiate", getMethodType(c_level, c_level, c_provider))
                .withBody(gen -> {
                    var wrapped = gen.param(0);
                    var provider = gen.param(1);

                    var wrapper = gen.newVar(c_ourLevel);

                    // $$WrappedLevel wrapper = ($$WrappedLevel) unsafe.allocateInstance(clazz);
                    gen.loadThis();
                    gen.fieldInsn(GETSTATIC, c_wrappedLevelFactory, "UNSAFE", c_unsafe);
                    gen.loadThis();
                    gen.getField(clazzField);
                    gen.methodInsn(INVOKEVIRTUAL, c_unsafe, "allocateInstance", getMethodType(c_object, c_class), false);
                    gen.typeInsn(CHECKCAST, c_ourLevel);
                    gen.store(wrapper);

                    var sideWrapped = gen.newVar(c_sidedLevel);
                    gen.load(wrapped);
                    gen.typeInsn(CHECKCAST, c_sidedLevel);
                    gen.store(sideWrapped);

                    // wrappedSetter.invokeExact(wrapper, wrapped);
                    gen.loadThis();
                    gen.getField(wrappedSetterField);
                    gen.load(wrapper);
                    gen.load(sideWrapped);
                    gen.methodInsn(INVOKEVIRTUAL, c_methodHandle, "invokeExact", getMethodType(VOID_TYPE, c_ourLevel, c_sidedLevel), false);

                    // providerSetter.invokeExact(wrapper, provider);
                    gen.loadThis();
                    gen.getField(providerSetterField);
                    gen.load(wrapper);
                    gen.load(provider);
                    gen.methodInsn(INVOKEVIRTUAL, c_methodHandle, "invokeExact", getMethodType(VOID_TYPE, c_ourLevel, c_provider), false);

                    // Unroll loop containing:
                    // field.setter.invokeExact(wrapper, field.getter.invokeExact(wrapped));
                    for (FieldPair field : fields) {
                        Type type = getType(field.field.getType());
                        gen.loadThis();
                        gen.getField(fieldBuilders.get(field.setter));
                        gen.load(wrapper);
                        gen.loadThis();
                        gen.getField(fieldBuilders.get(field.getter));
                        gen.load(sideWrapped);
                        gen.methodInsn(INVOKEVIRTUAL, c_methodHandle, "invokeExact", getMethodType(type, c_sidedLevel), false);
                        gen.methodInsn(INVOKEVIRTUAL, c_methodHandle, "invokeExact", getMethodType(VOID_TYPE, c_ourLevel, type), false);
                    }

                    gen.load(wrapper);
                    gen.ret();
                });

        Class<?> factoryClazz = define(cb);

        try {
            return (WrapperLevelFactory) factoryClazz.getConstructors()[0]
                    .newInstance(
                            clazz,
                            wrappedSetter,
                            providerSetter,
                            FastStream.of(fields).flatMap(e -> FastStream.of(e.getter, e.setter)).toArray(new MethodHandle[0])
                    );
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private static WrapperLevelFactory createFallbackFactory(Class<?> clazz, MethodHandle wrappedSetter, MethodHandle providerSetter, List<FieldPair> fieldPairs) {
        return new WrapperLevelFactory() {
            @Override
            public Level instantiate(Level wrapped, WrapperBlockProvider provider) {
                try {
                    Level wrapper = (Level) UNSAFE.allocateInstance(clazz);

                    wrappedSetter.invoke(wrapper, wrapped);
                    providerSetter.invoke(wrapper, provider);

                    for (FieldPair field : fieldPairs) {
                        field.setter.invoke(wrapper, field.getter.invoke(wrapped));
                    }
                    return wrapper;
                } catch (Throwable ex) {
                    throw new RuntimeException("Failed to instantiate wrapped level.", ex);
                }
            }
        };
    }

    private static Type sideName(String cName, Dist side) {
        var suffix = switch (side) {
            case CLIENT -> "$Client";
            case DEDICATED_SERVER -> "$Server";
        };
        return Type.getObjectType(cName + suffix);
    }

    private static Class<?> define(ClassBuilder cb) {
        return MultipartGenerator.MIXIN_COMPILER.defineClass(cb.name().getInternalName(), cb.build(CC_ClassWriter::new));
    }

    private record FieldPair(Field field, MethodHandle getter, MethodHandle setter) { }

    private static class TargetCollector {

        private final List<Field> copyOnConstruction = new ArrayList<>();
        private final List<Field> copyAtRuntime = new ArrayList<>();

        private final Set<MethodTarget> bridgedMethods = new LinkedHashSet<>();

        public void visitHierarchy(Class<?> clazz) {
            while (clazz != Object.class) {
                visit(clazz);
                clazz = clazz.getSuperclass();
            }
        }

        public void visit(Class<?> clazz) {
            for (Field field : clazz.getDeclaredFields()) {
                if (isStatic(field)) continue;

                // Copy all non-static fields on construction.
                copyOnConstruction.add(field);

                // Only copy public non-final fields at runtime.
                if (isFinal(field)) continue;
                if (isPublic(field)) {
                    copyAtRuntime.add(field);
                }
            }

            for (Method method : clazz.getDeclaredMethods()) {
                // We don't care about static methods.
                if (isStatic(method)) continue;
                // We can't do anything about final methods..
                if (isFinal(method)) continue;

                // Any public method we need to bridge.
                if (isPublic(method)) {
                    bridgedMethods.add(new MethodTarget(method.getName(), getType(method)));
                }
            }
        }

        private static boolean isStatic(Member member) {
            return Modifier.isStatic(member.getModifiers());
        }

        private static boolean isFinal(Member member) {
            return Modifier.isFinal(member.getModifiers());
        }

        private static boolean isPublic(Member member) {
            return Modifier.isPublic(member.getModifiers());
        }

        private static boolean isProtected(Member member) {
            return Modifier.isProtected(member.getModifiers());
        }
    }

    private record MethodTarget(String name, Type desc) { }
}
