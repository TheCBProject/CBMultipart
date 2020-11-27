package codechicken.multipart.api.annotation;

import codechicken.mixin.forge.TraitSide;

import java.lang.annotation.*;

/**
 * A trait!
 * Specifies that for the given target, the annotated class should be applied to said target
 * when the specified marker exists. where the MixinCompiler searches for the marker
 * is up to the target to decide and should be documented there.
 *
 * Created by covers1624 on 2/1/20.
 */
@Target (ElementType.TYPE)
@Retention (RetentionPolicy.RUNTIME)
@Repeatable (MultiPartTrait.TraitList.class)
public @interface MultiPartTrait {

    /**
     * The marker for the Trait.
     */
    Class<?> value();

    /**
     * The side for the Trait.
     */
    TraitSide side() default TraitSide.COMMON;

    /**
     * Created by covers1624 on 4/13/20.
     */
    @Target (ElementType.TYPE)
    @Retention (RetentionPolicy.RUNTIME)
    @interface TraitList {

        MultiPartTrait[] value();
    }
}
