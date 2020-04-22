package codechicken.mixin;

import codechicken.asm.ASMHelper;
import codechicken.mixin.api.MixinBackend;
import codechicken.mixin.api.MixinCompiler;
import codechicken.mixin.api.MixinLanguageSupport;
import codechicken.mixin.util.*;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.stream.Collectors;

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.*;

/**
 * Generates composite classes similar to the scala compiler with traits.
 *
 * Created by covers1624 on 2/11/20.
 */
@SuppressWarnings ("UnstableApiUsage")
public class MixinCompilerImpl implements MixinCompiler {

    private static final Logger logger = LogManager.getLogger("CodeChicken/MixinCompiler");
    private static final Level LOG_LEVEL = Level.getLevel(System.getProperty("codechicken.mixin.log_level", "DEBUG"));

    private final MixinBackend mixinBackend;
    private final List<LanguageSupportInstance> languageSupportInstances;
    private final List<MixinLanguageSupport> languageSupportList;
    private final Map<String, MixinLanguageSupport> languageSupportMap;

    private final Map<String, byte[]> traitByteMap = new HashMap<>();
    private final Map<String, ClassInfo> infoCache = new HashMap<>();
    private final Map<String, MixinInfo> mixinMap = new HashMap<>();

    public MixinCompilerImpl() {
        this(new MixinBackend.SimpleMixinBackend());
    }

    public MixinCompilerImpl(MixinBackend mixinBackend) {
        this.mixinBackend = mixinBackend;
        logger.log(LOG_LEVEL, "Starting CodeChicken MixinCompiler.");
        logger.log(LOG_LEVEL, "Loading MixinLanguageSupport services..");
        long start = System.nanoTime();
        SimpleServiceLoader<MixinLanguageSupport> langSupportLoader = new SimpleServiceLoader<>(MixinLanguageSupport.class);
        languageSupportInstances = langSupportLoader.poll().getNewServices().stream()//
                .map(LanguageSupportInstance::new)//
                .sorted(Comparator.comparingInt(e -> e.sortIndex))//
                .collect(Collectors.toList());
        languageSupportList = languageSupportInstances.stream()//
                .map(e -> e.instance)//
                .collect(Collectors.toList());
        Map<String, LanguageSupportInstance> languageSupportInstanceMap = new HashMap<>();
        for (LanguageSupportInstance instance : languageSupportInstances) {
            LanguageSupportInstance other = languageSupportInstanceMap.get(instance.name);
            if (other != null) {
                throw new RuntimeException(String.format("Duplicate MixinLanguageSupport. '%s' name conflicts with '%s'", instance, other));
            }
            languageSupportInstanceMap.put(instance.name, instance);
        }
        languageSupportMap = languageSupportInstanceMap.entrySet().stream()//
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().instance));
        long end = System.nanoTime();
        logger.log(LOG_LEVEL, "Loaded {} MixinLanguageSupport instances in {}.", languageSupportList.size(), Utils.timeString(start, end));
    }

    @Override
    public MixinBackend getMixinBackend() {
        return mixinBackend;
    }

    @Override
    @SuppressWarnings ("unchecked")
    public <T extends MixinLanguageSupport> Optional<T> findLanguageSupport(String name) {
        return (Optional<T>) Optional.ofNullable(languageSupportMap.get(name));
    }

    @Override
    public ClassInfo getClassInfo(String name) {
        return infoCache.computeIfAbsent(name, this::obtainInfo);
    }

    @Override
    public MixinInfo getMixinInfo(String name) {
        return mixinMap.get(name);
    }

    @Override
    @SuppressWarnings ("unchecked")
    public <T> Class<T> compileMixinClass(String name, String superClass, Set<String> traits) {
        ClassInfo baseInfo = getClassInfo(superClass);
        if (traits.isEmpty()) {
            return (Class<T>) mixinBackend.loadClass(baseInfo.getName().replace('/', '.'));
        }
        long start = System.nanoTime();
        List<MixinInfo> baseTraits = traits.stream()//
                .map(mixinMap::get)//
                .collect(Collectors.toList());
        List<MixinInfo> mixinInfos = baseTraits.stream()//
                .flatMap(MixinInfo::linearize)//
                .distinct()//
                .collect(Collectors.toList());
        List<ClassInfo> traitInfos = mixinInfos.stream()//
                .map(MixinInfo::getName)//
                .map(this::getClassInfo)//
                .collect(Collectors.toList());
        ClassNode cNode = new ClassNode();

        cNode.visit(V1_8, ACC_PUBLIC, name, null, superClass, baseTraits.stream().map(MixinInfo::getName).toArray(String[]::new));

        MethodInfo cInit = baseInfo.getMethods().filter(e -> e.getName().equals("<init>")).findFirst().orElseThrow(IllegalStateException::new);
        MethodNode mInit = (MethodNode) cNode.visitMethod(ACC_PUBLIC, "<init>", cInit.getDesc(), null, null);
        Utils.writeBridge(mInit, cInit.getDesc(), INVOKESPECIAL, superClass, "<init>", cInit.getDesc());
        mInit.instructions.remove(mInit.instructions.getLast());//remove the RETURN from writeBridge

        List<MixinInfo> prevInfos = new ArrayList<>();

        for (MixinInfo t : mixinInfos) {
            mInit.visitVarInsn(ALOAD, 0);
            mInit.visitMethodInsn(INVOKESTATIC, t.getName(), "$init$", "(L" + t.getName() + ";)V", false);

            for (FieldMixin f : t.getFields()) {
                FieldNode fv = (FieldNode) cNode.visitField(ACC_PRIVATE, f.getAccessName(t.getName()), f.getDesc(), null, null);

                Type fType = Type.getType(fv.desc);
                MethodVisitor mv;
                mv = cNode.visitMethod(ACC_PUBLIC, fv.name, "()" + f.getDesc(), null, null);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, name, fv.name, fv.desc);
                mv.visitInsn(fType.getOpcode(IRETURN));
                mv.visitMaxs(-1, -1);

                mv = cNode.visitMethod(ACC_PUBLIC, fv.name + "_$eq", "(" + f.getDesc() + ")V", null, null);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(fType.getOpcode(ILOAD), 1);
                mv.visitFieldInsn(PUTFIELD, name, fv.name, fv.desc);
                mv.visitInsn(RETURN);
                mv.visitMaxs(-1, -1);
            }

            for (String s : t.getSupers()) {
                int nIdx = s.indexOf('(');
                String sName = s.substring(0, nIdx);
                String sDesc = s.substring(nIdx);
                MethodNode mv = (MethodNode) cNode.visitMethod(ACC_PUBLIC, t.getName().replace("/", "$") + "$$super$" + sName, sDesc, null, null);

                Optional<MixinInfo> prev = Lists.reverse(prevInfos).stream()//
                        .filter(e -> e.getMethods().stream().anyMatch(m -> m.name.equals(sName) && m.desc.equals(sDesc)))//
                        .findFirst();
                //each super goes to the one before
                if (prev.isPresent()) {
                    Utils.writeStaticBridge(mv, sName, prev.get());
                } else {
                    Utils.writeBridge(mv, sDesc, INVOKESPECIAL, baseInfo.findPublicImpl(sName, sDesc).orElseThrow(IllegalStateException::new).getOwner().getName(), sName, sDesc);
                }

            }
            prevInfos.add(t);
        }
        mInit.visitInsn(RETURN);

        Set<String> methodSigs = new HashSet<>();
        for (MixinInfo t : Lists.reverse(mixinInfos)) {//last trait gets first pick on methods
            for (MethodNode m : t.getMethods()) {
                if (methodSigs.add(m.name + m.desc)) {
                    MethodNode mv = (MethodNode) cNode.visitMethod(ACC_PUBLIC, m.name, m.desc, null, m.exceptions.toArray(new String[0]));
                    Utils.writeStaticBridge(mv, m.name, t);
                }
            }
        }

        //generate synthetic bridge methods for covariant return types
        Set<ClassInfo> allParentInfos = Utils.of(baseInfo, traitInfos).stream()//
                .flatMap(Utils::allParents)//
                .collect(Collectors.toSet());
        List<MethodInfo> allParentMethods = allParentInfos.stream()//
                .flatMap(ClassInfo::getMethods)//
                .collect(Collectors.toList());

        for (String nameDesc : new HashSet<>(methodSigs)) {
            int nIdx = nameDesc.indexOf('(');
            String sName = nameDesc.substring(0, nIdx);
            String sDesc = nameDesc.substring(nIdx);
            String pDesc = sDesc.substring(0, sDesc.lastIndexOf(")") + 1);
            allParentMethods.stream().filter(m -> m.getName().equals(sName) && m.getDesc().startsWith(pDesc)).forEach(m -> {
                if (methodSigs.add(m.getName() + m.getDesc())) {
                    MethodNode mv = (MethodNode) cNode.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC | ACC_BRIDGE, m.getName(), m.getDesc(), null, m.getExceptions());
                    Utils.writeBridge(mv, mv.desc, INVOKEVIRTUAL, cNode.name, sName, sDesc);
                }
            });
        }

        byte[] bytes = ASMHelper.createBytes(cNode, COMPUTE_FRAMES | COMPUTE_MAXS);
        long end = System.nanoTime();
        logger.log(LOG_LEVEL, "Generation of {} with [{}] took {}", superClass, String.join(", ", traits), Utils.timeString(start, end));
        return (Class<T>) defineClass(name, bytes);
    }

    @Override
    public Class<?> defineClass(String name, byte[] bytes) {
        String asmName = Utils.asmName(name);
        traitByteMap.put(asmName, bytes);
        infoCache.remove(asmName);
        return mixinBackend.defineClass(name, bytes);

    }

    @Override
    public MixinInfo registerTrait(ClassNode cNode) {
        for (MixinLanguageSupport languageSupport : languageSupportList) {
            Optional<MixinInfo> opt = languageSupport.buildMixinTrait(cNode);
            if (!opt.isPresent()) {
                continue;
            }
            MixinInfo info = opt.get();
            mixinMap.put(info.getName(), info);
            return info;
        }
        throw new IllegalStateException("No MixinLanguageSupport wished to handle class '" + cNode.name + "'");
    }

    private ClassInfo obtainInfo(String name) {
        if (name == null) {
            return null;
        }
        ClassNode cNode = getClassNode(name);
        for (MixinLanguageSupport languageSupport : languageSupportList) {
            Optional<ClassInfo> info = languageSupport.obtainInfo(name, cNode);
            if (info.isPresent()) {
                return info.get();
            }
        }
        return null;
    }

    @Override
    public ClassNode getClassNode(String name) {
        if (name.equals("java/lang/Object")) {
            return null;
        }
        byte[] bytes = traitByteMap.computeIfAbsent(name, mixinBackend::getBytes);
        if (bytes == null) {
            return null;
        }
        return ASMHelper.createClassNode(bytes, EXPAND_FRAMES);
    }

    private class LanguageSupportInstance {

        private final Class<? extends MixinLanguageSupport> clazz;
        private final MixinLanguageSupport instance;
        private final String name;
        private final int sortIndex;

        public LanguageSupportInstance(Class<? extends MixinLanguageSupport> clazz) {
            this.clazz = clazz;
            MixinLanguageSupport.LanguageName lName = clazz.getAnnotation(MixinLanguageSupport.LanguageName.class);
            MixinLanguageSupport.SortingIndex sIndex = clazz.getAnnotation(MixinLanguageSupport.SortingIndex.class);
            if (lName == null) {
                throw new RuntimeException("MixinLanguageSupport '" + clazz.getName() + "' is not annotated with MixinLanguageSupport.LanguageName!");
            }
            name = lName.value();
            sortIndex = sIndex != null ? sIndex.value() : 1000;

            logger.log(LOG_LEVEL, "Loading MixinLanguageSupport '{}', Name: '{}', Sorting Index: '{}'", clazz.getName(), name, sortIndex);
            Optional<MixinLanguageSupport> instance = Utils.findConstructor(clazz, MixinCompiler.class)//
                    .map(c -> Utils.newInstance(c, MixinCompilerImpl.this));
            this.instance = instance.orElseGet(() ->//
                    Utils.findConstructor(clazz)//
                            .map(Utils::newInstance)//
                            .orElseThrow(RuntimeException::new)//
            );
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", LanguageSupportInstance.class.getSimpleName() + "[", "]")//
                    .add("class=" + clazz.getName())//
                    .add("name='" + name + "'")//
                    .add("sortIndex=" + sortIndex)//
                    .toString();
        }
    }

}
