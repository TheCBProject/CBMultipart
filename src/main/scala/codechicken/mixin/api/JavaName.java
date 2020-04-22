package codechicken.mixin.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a member as using a standard java name E.g: 'java.lang.Object'
 * Simply a marker for sanity sake.
 * Created by covers1624 on 2/17/20.
 */
@Retention (RetentionPolicy.SOURCE)
@Target ({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
public @interface JavaName {}
