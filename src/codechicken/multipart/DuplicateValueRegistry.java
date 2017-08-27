package codechicken.multipart;

import codechicken.lib.reflect.ObfMapping;
import codechicken.lib.reflect.ReflectionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings ("unchecked")
public class DuplicateValueRegistry extends RegistryNamespaced {

    private final RegistryNamespaced wrapped;
    private final HashMap<Object, ResourceLocation> classMap = new HashMap<>();

    public DuplicateValueRegistry(RegistryNamespaced<ResourceLocation, Class<? extends TileEntity>> wrapped) {
        this.wrapped = wrapped;
        {//Set the underlying maps of our instance to those of the wrapped instance, just in case people are dumb and access the field directly.
            ObfMapping registryObjects = new ObfMapping("net/minecraft/util/registry/RegistrySimple", "field_82596_a");
            ObfMapping underlyingIMap = new ObfMapping("net/minecraft/util/registry/RegistryNamespaced", "field_148759_a");
            ObfMapping iObjectReg = new ObfMapping("net/minecraft/util/registry/RegistryNamespaced", "field_148758_b");
            ReflectionManager.setField(registryObjects, this, ReflectionManager.getField(registryObjects, wrapped, Map.class));
            ReflectionManager.setField(underlyingIMap, this, ReflectionManager.getField(underlyingIMap, wrapped, IntIdentityHashBiMap.class));
            ReflectionManager.setField(iObjectReg, this, ReflectionManager.getField(iObjectReg, wrapped, Map.class));
        }
    }

    @Nullable
    @Override
    public ResourceLocation getNameForObject(Object value) {
        if (classMap.containsKey(value)) {
            return classMap.get(value);
        }
        return (ResourceLocation) wrapped.getNameForObject(value);
    }

    public void addMapping(Object clazz, ResourceLocation mapping) {
        classMap.put(clazz, mapping);
    }

    //@formatter:off
    @Override public Object getObject(@Nullable Object name) {return wrapped.getObject(name);}
    @Override public void putObject(Object key, Object value) {wrapped.putObject(key, value);}
    @Override public Set getKeys() {return wrapped.getKeys();}
    @Override public Object getRandomObject(Random random) {return wrapped.getRandomObject(random);}
    @Override public boolean containsKey(Object key) {return wrapped.containsKey(key);}
    @Override public Iterator iterator() {return wrapped.iterator();}
    @Override public void register(int id, Object key, Object value) {wrapped.register(id, key, value);}
    @Override public int getIDForObject(@Nullable Object value) {return wrapped.getIDForObject(value);}
    @Override public Object getObjectById(int id) {return wrapped.getObjectById(id);}
    //@formatter:on
}
