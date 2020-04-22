package codechicken.mixin.util;

/**
 * Created by covers1624 on 2/11/20.
 */
public interface MethodInfo {

    ClassInfo getOwner();

    String getName();

    String getDesc();

    String[] getExceptions();

    boolean isPrivate();

    boolean isAbstract();
}
