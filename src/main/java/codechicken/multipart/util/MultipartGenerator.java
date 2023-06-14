package codechicken.multipart.util;

import codechicken.asm.ASMHelper;
import codechicken.asm.CC_ClassWriter;
import codechicken.asm.ObfMapping;
import codechicken.mixin.api.*;
import codechicken.mixin.forge.ForgeMixinBackend;
import codechicken.mixin.forge.SidedGenerator;
import codechicken.mixin.util.JavaTraitGenerator;
import codechicken.mixin.util.SimpleDebugger;
import codechicken.mixin.util.Utils;
import codechicken.multipart.api.annotation.MultiPartTrait;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.init.CBMultipartModContent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import net.covers1624.quack.collection.FastStream;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static codechicken.mixin.util.Utils.asmName;
import static net.covers1624.quack.util.SneakyUtils.throwUnchecked;
import static org.objectweb.asm.Opcodes.*;

/**
 * Please use {@link MultiPartTrait} annotations.
 * <p>
 * TODO allow parallel registration of traits. Requires mixin compiler changes. (parallel stream processing of annotations)
 * Created by covers1624 on 4/5/20.
 */
public class MultipartGenerator extends SidedGenerator<TileMultipart, MultipartGenerator.Factory, MultiPart> {

    private static final Logger logger = LogManager.getLogger();
    private static final CrashLock LOCK = new CrashLock("Already initialized.");
    @Nullable
    private static final SimpleDebugger.DumpType DEBUG_TYPE = parseType(System.getProperty("codechicken.multipart.debug", null));

    public static final MultipartGenerator INSTANCE = new MultipartGenerator();
    public static final MixinCompiler MIXIN_COMPILER = INSTANCE.getMixinCompiler();

    private final Map<String, TraitKey> passthroughTraits = new HashMap<>();

    private final MixinFactory.TraitKey clientTrait = registerTrait(asmName("codechicken.multipart.trait.TileMultipartClient"));

    private MultipartGenerator() {
        super(MixinCompiler.create(new ForgeMixinBackend(), makeDebugger(), getMixinSupports()), TileMultipart.class, Factory.class, "cmp");
        Optional<MixinLanguageSupport.JavaMixinLanguageSupport> javaSupport = mixinCompiler.findLanguageSupport("java");
        javaSupport//
                .orElseThrow(() -> new RuntimeException("Unable to find JavaMixinLanguageSupport instance..."))//
                .setTraitGeneratorFactory(MultipartJavaTraitGenerator::new);
    }

    /**
     * Overload for {@link #registerPassThroughInterface(String, boolean, boolean)},
     * passing true to both boolean parameters.
     *
     * @param iFace The interface.
     */
    @AsmName
    @JavaName
    public void registerPassThroughInterface(String iFace) {
        registerPassThroughInterface(iFace, true, true);
    }

    /**
     * A passthrough interface is an interface to be implemented on the container tile instance, for which all calls are passed
     * directly through to a single implementing part.
     * Registering a passthrough interface is equivalent to defining a mixin trait as follows.
     * 1. field 'impl' which contains a reference to the corresponding part.
     * 2. occlusionTest is overriden to prevent more than one part with <code>iFace</code> existing in the block space.
     * 3. implementing <code>iFace</code> and passing all calls directly to the part instance.
     * <p>
     * This allows compatibility with APIs that expect interfaces on the tile entity.
     * If you require more than one part in the space implementing an interface, i.e the old IInventory system.
     * You will need to write your own trait implementation manually. Refer to {@link codechicken.multipart.trait.TInventoryTile}.
     *
     * @param iFace  The Interface to implement.
     * @param client If this interface should be used client side.
     * @param server If this interface should be used server side.
     */
    @AsmName
    @JavaName
    public void registerPassThroughInterface(String iFace, boolean client, boolean server) {
        iFace = Utils.asmName(iFace);
        MixinFactory.TraitKey key = registerPassthroughTrait(iFace);
        String tName = key.getTName();
        registerTrait(iFace, client ? tName : null, server ? tName : null);
    }

    //Internal, loads all MultiPartTrait annotations.
    public void loadAnnotations() {
        LOCK.lock();
        loadAnnotations(MultiPartTrait.class, MultiPartTrait.TraitList.class);
    }

    public ImmutableSet<MixinFactory.TraitKey> getTraits(MultiPart part, boolean client) {
        return getTraits(Collections.singleton(part), client);
    }

    public ImmutableSet<MixinFactory.TraitKey> getTraits(Collection<MultiPart> parts, boolean client) {
        return FastStream.concat(
                client ? FastStream.of(clientTrait) : FastStream.of(),
                FastStream.of(parts).flatMap(e -> getTraitsForObject(e, client))
        ).toImmutableSet();
    }

    public TileMultipart generateCompositeTile(@Nullable BlockEntity tile, BlockPos pos, Collection<MultiPart> parts, boolean client) {
        ImmutableSet<MixinFactory.TraitKey> traits = getTraits(parts, client);
        if (tile instanceof TileMultipart && traits.equals(getTraitsForClass(tile.getClass()))) {
            return (TileMultipart) tile;
        }
        return construct(traits).newInstance(pos, CBMultipartModContent.MULTIPART_BLOCK.get().defaultBlockState());
    }

    public TraitKey registerPassthroughTrait(@AsmName String iName) {
        TraitKey key = passthroughTraits.get(iName);
        if (key != null) {
            return key;
        }
        String simpleName = iName.substring(iName.lastIndexOf('/') + 1);
        if (simpleName.startsWith("I") && (simpleName.length() > 1 && Character.isUpperCase(simpleName.charAt(1)))) {
            simpleName = simpleName.substring(1);
        }
        String tName = "T" + simpleName + "$$PassThrough";

        String vName = "impl";
        String iDesc = "L" + iName + ";";

        ClassNode iNode = mixinCompiler.getClassNode(iName);
        if (iNode == null) {
            throwUnchecked(new ClassNotFoundException("Unable to generate PassThrough trait for interface: " + iName + ", class not found."));
            return null;//Unreachable
        }
        if ((iNode.access & ACC_INTERFACE) == 0) {
            throw new IllegalArgumentException("Class: " + iName + ", is not an interface.");
        }

        ClassWriter cw = new CC_ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        cw.visit(V1_8, ACC_SUPER, tName, null, "codechicken/multipart/block/TileMultipart", new String[] { iName });

        {
            FieldVisitor fv = cw.visitField(ACC_PRIVATE, vName, iDesc, null, null);
            fv.visitEnd();
        }
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "codechicken/multipart/block/TileMultipart", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(-1, -1);
        }

        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "bindPart", "(Lcodechicken/multipart/api/part/MultiPart;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, "codechicken/multipart/block/TileMultipart", "bindPart", "(Lcodechicken/multipart/api/part/MultiPart;)V", false);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(INSTANCEOF, iName);
            Label l2 = new Label();
            mv.visitJumpInsn(IFEQ, l2);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, iName);
            mv.visitFieldInsn(PUTFIELD, tName, vName, iDesc);
            mv.visitLabel(l2);
            mv.visitInsn(RETURN);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }

        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "partRemoved", "(Lcodechicken/multipart/api/part/MultiPart;I)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitMethodInsn(INVOKESPECIAL, "codechicken/multipart/block/TileMultipart", "partRemoved", "(Lcodechicken/multipart/api/part/MultiPart;I)V", false);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, tName, vName, iDesc);
            Label l2 = new Label();
            mv.visitJumpInsn(IF_ACMPNE, l2);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(ACONST_NULL);
            mv.visitFieldInsn(PUTFIELD, tName, vName, iDesc);
            mv.visitLabel(l2);
            mv.visitInsn(RETURN);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "canAddPart", "(Lcodechicken/multipart/api/part/MultiPart;)Z", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, tName, vName, iDesc);
            Label l1 = new Label();
            mv.visitJumpInsn(IFNULL, l1);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(INSTANCEOF, iName);
            mv.visitJumpInsn(IFEQ, l1);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(IRETURN);
            mv.visitLabel(l1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, "codechicken/multipart/block/TileMultipart", "canAddPart", "(Lcodechicken/multipart/api/part/MultiPart;)Z", false);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }

        methods(iNode).forEach(m -> {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0]));
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, tName, vName, iDesc);
            Utils.finishBridgeCall(mv, m.desc, INVOKEINTERFACE, iName, m.name, m.desc, true);
        });
        cw.visitEnd();
        mixinCompiler.defineInternal(tName, cw.toByteArray());
        key = registerTrait(tName);
        passthroughTraits.put(iName, key);
        return key;
    }

    private Collection<MethodNode> methods(ClassNode cNode) {
        return getAllMethods(new HashSet<>(), cNode).collect(Collectors.toMap(Pair::getLeft, Pair::getRight)).values();
    }

    private Stream<Pair<String, MethodNode>> getAllMethods(Set<String> visited, ClassNode cNode) {
        Stream<Pair<String, MethodNode>> methods = cNode.methods.stream().map(m -> Pair.of(m.name + m.desc, m));
        if (visited.add(cNode.name)) {
            if (!cNode.interfaces.isEmpty()) {
                Stream<Pair<String, MethodNode>> others = cNode.interfaces.stream()
                        .filter(visited::add)
                        .map(mixinCompiler::getClassNode)
                        .flatMap(e -> getAllMethods(visited, e));
                return Streams.concat(methods, others);
            }
            return methods;
        }
        return Stream.empty();
    }

    private static class MultipartJavaTraitGenerator extends JavaTraitGenerator {

        public MultipartJavaTraitGenerator(MixinCompiler mixinCompiler, ClassNode cNode) {
            super(mixinCompiler, cNode);
        }

        @Override
        protected void preCheckNode() {
            //Override interface warning from parent, with information about PassthroughInterfaces.
            if ((cNode.access & ACC_INTERFACE) != 0) {
                throw new IllegalArgumentException("Cannot register java interface '" + cNode.name + "' as a mixin trait. Try as a PassthroughInterface.");
            }
        }

        @Override
        protected void beforeTransform() {
            ObfMapping m_copyFrom = new ObfMapping(cNode.name, "copyFrom", "(Lcodechicken/multipart/block/TileMultipart;)V");
            if (!instanceFields.isEmpty() && (ASMHelper.findMethod(m_copyFrom, cNode) == null)) {
                MethodVisitor mv = m_copyFrom.visitMethod(cNode, ACC_PUBLIC, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitMethodInsn(INVOKESPECIAL, "codechicken/multipart/block/TileMultipart", m_copyFrom.s_name, m_copyFrom.s_desc, false);

                mv.visitVarInsn(ALOAD, 1);
                mv.visitTypeInsn(INSTANCEOF, cNode.name);
                Label end = new Label();
                mv.visitJumpInsn(IFEQ, end);

                instanceFields.forEach(f -> {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitFieldInsn(GETFIELD, cNode.name, f.name, f.desc);
                    mv.visitFieldInsn(PUTFIELD, cNode.name, f.name, f.desc);
                });

                mv.visitLabel(end);
                mv.visitInsn(RETURN);
                mv.visitMaxs(-1, -1);//ClassWriter.COMPUTE_MAXS
                mv.visitEnd();
            }
        }
    }

    public interface Factory {

        TileMultipart newInstance(BlockPos pos, BlockState state);
    }

    private static SimpleDebugger.DumpType parseType(@Nullable String value) {
        if (value == null) {
            return null;
        }
        return SimpleDebugger.DumpType.valueOf(value);
    }

    private static MixinDebugger makeDebugger() {
        if (DEBUG_TYPE == null) {
            return new MixinDebugger.NullDebugger();
        }
        return new SimpleDebugger(Paths.get("./asm/multipart"), DEBUG_TYPE);
    }

    @Deprecated //Remove in 1.16.3
    private static Collection<Class<? extends MixinLanguageSupport>> getMixinSupports() {
        // TODO load the Scala language support if scala is present.
        return ImmutableList.of(MixinLanguageSupport.JavaMixinLanguageSupport.class);
    }
}
