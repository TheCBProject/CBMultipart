package codechicken.microblock.api;

import codechicken.microblock.Microblock;
import codechicken.mixin.forge.TraitSide;

import java.lang.annotation.*;

/**
 * A Trait applied to {@link Microblock} part instances when markers exist on {@link MicroMaterial}.
 * <p>
 * When {@link MicroBlockTrait#value()} is found on a {@link MicroMaterial} instance's class hierarchy,
 * mixes the target of this interface into the {@link Microblock} part implementation containing the
 * {@link MicroMaterial}.
 * <p>
 * Created by covers1624 on 4/18/20.
 */
@Target (ElementType.TYPE)
@Retention (RetentionPolicy.RUNTIME)
@Repeatable (MicroBlockTrait.TraitList.class)
public @interface MicroBlockTrait {

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

        MicroBlockTrait[] value();
    }
}
