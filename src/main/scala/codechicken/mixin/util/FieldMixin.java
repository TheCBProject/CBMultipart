package codechicken.mixin.util;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;

/**
 * Created by covers1624 on 2/11/20.
 */
public class FieldMixin {

    private final String name;
    private final String desc;
    private final int access;

    public FieldMixin(String name, String desc, int access) {
        this.name = name;
        this.desc = desc;
        this.access = access;
    }

    public String getAccessName(String owner) {
        if ((access & ACC_PRIVATE) != 0) {
            return owner.replace("/", "$") + "$$" + name;
        }
        return name;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public int getAccess() {
        return access;
    }
}
