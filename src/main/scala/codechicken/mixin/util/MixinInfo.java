package codechicken.mixin.util;

import com.google.common.collect.Streams;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by covers1624 on 2/11/20.
 */
@SuppressWarnings ("UnstableApiUsage")
public class MixinInfo {

    private final String name;
    private final String parent;
    private final List<MixinInfo> parentTraits;
    private final List<FieldMixin> fields;
    private final List<MethodNode> methods;
    private final List<String> supers;

    public MixinInfo(String name, String parent, List<MixinInfo> parentTraits, List<FieldMixin> fields, List<MethodNode> methods, List<String> supers) {
        this.name = name;
        this.parent = parent;
        this.parentTraits = parentTraits;
        this.fields = fields;
        this.methods = methods;
        this.supers = supers;
    }

    public Stream<MixinInfo> linearize() {
        return Streams.concat(//
                parentTraits.stream().flatMap(MixinInfo::linearize),//
                Stream.of(this)//
        );

    }

    public String getTName() {
        return getName(); //+ "$class"
    }

    public String getName() {
        return name;
    }

    public String getParent() {
        return parent;
    }

    public List<MixinInfo> getParentTraits() {
        return parentTraits;
    }

    public List<FieldMixin> getFields() {
        return fields;
    }

    public List<MethodNode> getMethods() {
        return methods;
    }

    public List<String> getSupers() {
        return supers;
    }
}
