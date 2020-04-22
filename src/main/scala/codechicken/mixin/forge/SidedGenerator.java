package codechicken.mixin.forge;

import codechicken.mixin.MixinFactoryImpl;
import codechicken.mixin.api.AsmName;
import codechicken.mixin.api.JavaName;
import codechicken.mixin.api.MixinCompiler;
import codechicken.mixin.api.MixinFactory;
import codechicken.mixin.util.Utils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.*;
import java.util.stream.Stream;

import static codechicken.mixin.util.Utils.asmName;

/**
 * Created by covers1624 on 4/14/20.
 */
@SuppressWarnings ("UnstableApiUsage")//We are careful
public class SidedGenerator<B, T> extends MixinFactoryImpl<B> {

    private static final Logger logger = LogManager.getLogger();

    protected final Map<String, MixinFactory.TraitKey> clientTraits = new HashMap<>();
    protected final Map<String, MixinFactory.TraitKey> serverTraits = new HashMap<>();

    protected final Map<Class<?>, ImmutableSet<MixinFactory.TraitKey>> clientObjectTraitCache = new HashMap<>();
    protected final Map<Class<?>, ImmutableSet<MixinFactory.TraitKey>> serverObjectTraitCache = new HashMap<>();

    protected SidedGenerator(MixinCompiler mixinCompiler, Class<B> baseType, String classSuffix, Class<?>... ctorParamTypes) {
        super(mixinCompiler, baseType, classSuffix, ctorParamTypes);
    }

    /**
     * Overload of {@link #registerTrait(String, String, String)}, using the same
     * trait impl for client and server.
     *
     * @param marker The Marker class, to be found in the part instances class hierarchy.
     */
    @AsmName
    @JavaName
    public void registerTrait(String marker, String trait) {
        registerTrait(marker, trait, trait);
    }

    /**
     * Registers a trait to be applied to the host tile in the presence of a specific
     * marker class existing in the class hierarchy of a part instance.
     *
     * @param marker      The Marker class, to be found in the part instances class hierarchy.
     * @param clientTrait The trait class to be applied on the client side.
     * @param serverTrait The trait class to be applied on the server side.
     */
    @AsmName
    @JavaName
    public void registerTrait(String marker, String clientTrait, String serverTrait) {
        marker = asmName(marker);

        if (clientTrait != null) {
            registerSide(clientTraits, marker, asmName(clientTrait));
        }

        if (serverTrait != null) {
            registerSide(serverTraits, marker, asmName(serverTrait));
        }
    }

    /**
     * Gets all the {@link TraitKey}'s this generator knows about from the <code>thing</code>'s
     * class hierarchy.
     *
     * @param thing  The thing to get all traits from.
     * @param client If this is the client side or not.
     * @return The {@link TraitKey}s.
     */
    public ImmutableSet<MixinFactory.TraitKey> getTraitsForObject(T thing, boolean client) {
        return getObjectTraitCache(client).computeIfAbsent(thing.getClass(), clazz -> {
            Map<String, MixinFactory.TraitKey> traits = getTraitMap(client);
            return hierarchy(clazz)//
                    //.parallel()// TODO, maybe?
                    .map(Utils::asmName)//
                    .map(traits::get)//
                    .filter(Objects::nonNull)//
                    .collect(ImmutableSet.toImmutableSet());
        });
    }

    protected void loadAnnotations(Class<? extends Annotation> aClass, Class<? extends Annotation> aListClass) {
        Type aType = Type.getType(aClass);
        Type lType = Type.getType(aListClass);
        ModList.get().getAllScanData().stream()//
                .map(ModFileScanData::getAnnotations)//
                .flatMap(Collection::stream)//
                .filter(a -> a.getAnnotationType().equals(aType) || a.getAnnotationType().equals(lType))//
                .filter(a -> a.getTargetType() == ElementType.TYPE)//
                .map(a -> {
                    if (a.getAnnotationType().equals(lType)) {
                        @SuppressWarnings ("unchecked")
                        List<Map<String, Object>> entries = ((List<Map<String, Object>>) a.getAnnotationData().get("value"));
                        return Pair.of(a, entries);
                    }
                    return Pair.of(a, Collections.singletonList(a.getAnnotationData()));
                })//
                .forEach(p -> {
                    ModFileScanData.AnnotationData a = p.getLeft();
                    List<Map<String, Object>> dataList = p.getRight();
                    String tName = a.getClassType().getInternalName();
                    logger.info("Trait: {}", tName);
                    for (Map<String, Object> data : dataList) {
                        Type marker = (Type) data.get("value");
                        ModAnnotation.EnumHolder holder = (ModAnnotation.EnumHolder) data.get("side");
                        TraitSide side = holder != null ? TraitSide.valueOf(holder.getValue()) : TraitSide.COMMON;
                        logger.info("    Marker: {}, Side: {}", marker.getInternalName(), side);
                        if (side.isSupported()) {
                            if (side.isCommon() || side.isClient()) {
                                registerSide(clientTraits, marker.getInternalName(), tName);
                            }
                            if (side.isCommon() || side.isServer()) {
                                registerSide(serverTraits, marker.getInternalName(), tName);
                            }
                        }
                    }
                });
    }

    protected Stream<Class<?>> hierarchy(Class<?> clazz) {
        return Streams.concat(//
                Stream.of(clazz),//
                Arrays.stream(clazz.getInterfaces()).flatMap(this::hierarchy),//
                Streams.stream(Optional.ofNullable(clazz.getSuperclass())).flatMap(this::hierarchy)//
        );
    }

    protected Map<String, MixinFactory.TraitKey> getTraitMap(boolean client) {
        return client ? clientTraits : serverTraits;
    }

    protected Map<Class<?>, ImmutableSet<MixinFactory.TraitKey>> getObjectTraitCache(boolean client) {
        return client ? clientObjectTraitCache : serverObjectTraitCache;
    }

    protected void registerSide(Map<String, MixinFactory.TraitKey> map, String marker, String trait) {
        MixinFactory.TraitKey existing = map.get(marker);
        MixinFactory.TraitKey newTrait = registerTrait(trait);
        if (existing != null) {
            if (!existing.equals(newTrait)) {
                logger.error("Attempted to re-register trait for '{}', with a different impl, Ignoring. Existing: '{}', New: '{}'", marker, existing.getTName(), newTrait.getTName());
                return;
            } else if (existing.equals(newTrait)) {
                logger.debug("Skipping re-register of trait for '{}', same impl detected.", marker);
                return;
            }
        }
        map.put(marker, newTrait);
    }
}
