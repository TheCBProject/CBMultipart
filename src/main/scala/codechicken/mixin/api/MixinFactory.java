package codechicken.mixin.api;

import codechicken.mixin.util.Utils;
import com.google.common.collect.ImmutableSet;

import java.util.function.BiConsumer;

/**
 * Represents a 'user' facing interface for interacting and caching the {@link MixinCompiler}.
 *
 * Created by covers1624 on 2/17/20.
 */
public interface MixinFactory<B> {

    /**
     * Gets the associated MixinCompiler for this MixinFactory.
     *
     * @return The MixinCompiler.
     */
    MixinCompiler getMixinCompiler();

    /**
     * Registers a trait with the given class name.
     *
     * @param tName The ClassName for the trait.
     * @return a TraitKey for using this registered trait.
     */
    TraitKey registerTrait(@AsmName String tName);

    /**
     * Registers a trait.
     *
     * @param trait The Class for the trait.
     * @return a TraitKey for using this registered trait.
     */
    default TraitKey registerTrait(Class<?> trait) {
        return registerTrait(Utils.asmName(trait));
    }

    /**
     * Constructs a new {@link B} instance with the given set of traits applied.
     *
     * The MixinFactory will cache constructed classes with the given set of traits,
     * and uses Generated lambda classes to call its constructor, offering incredibly
     * little performance penalty for construction. (TODO)
     *
     * It should be noted that, {@link ImmutableSet} is explicitly used here,
     * as their hashCode is statically computed, making it favourable for use as a
     * key in a Map.
     *
     * @param traits The traits to apply.
     * @param args   Constructor arguments for the {@link B} instance constructor.
     * @return The new instance.
     */
    B construct(ImmutableSet<TraitKey> traits, Object... args);

    /**
     * Gets the traits that were used in compiling the given class.
     * If the given class was not compiled by this factory, simply returns null.
     *
     * @param clazz The Class to get the traits for.
     * @return The classes traits, or null if it was not compiled by this factory.
     */
    ImmutableSet<TraitKey> getTraitsForClass(Class<?> clazz);

    /**
     * Adds a callback to be run when a new trait class is compiled.
     *
     * @param callback The callback.
     */
    void addCompilationCallback(BiConsumer<Class<? extends B>, ImmutableSet<TraitKey>> callback);

    interface TraitKey {

        /**
         * Gets the class name for this trait.
         *
         * @return The class name.
         */
        @AsmName
        String getTName();
    }

}
