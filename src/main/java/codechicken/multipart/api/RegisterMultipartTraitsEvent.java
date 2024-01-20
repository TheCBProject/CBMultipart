package codechicken.multipart.api;

import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.util.MultipartGenerator;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

/**
 * Fired on the mod bus for mods to register their traits and passthrough interfaces
 * for {@link TileMultipart} classes.
 * <p>
 * This is fired at the end of mod loading, from {@link FMLLoadCompleteEvent}.
 * <p>
 * Created by covers1624 on 20/1/24.
 */
public final class RegisterMultipartTraitsEvent extends Event implements IModBusEvent {

    private final MultipartGenerator generator;

    public RegisterMultipartTraitsEvent(MultipartGenerator generator) {
        this.generator = generator;
    }

    /**
     * Register {@code trait} to be mixed into the {@link TileMultipart} when
     * {@code marker} is found implemented on a {@link MultiPart} instance.
     *
     * @param marker The part marker class.
     * @param trait  The trait to implement.
     */
    public void registerTrait(Class<?> marker, Class<? extends TileMultipart> trait) {
        generator.registerTrait(marker, trait);
    }

    /**
     * The same as {@link #registerTrait(Class, Class)} however, only effective client side.
     *
     * @param marker The part marker class.
     * @param trait  The trait to implement.
     */
    public void registerClientTrait(Class<?> marker, Class<? extends TileMultipart> trait) {
        generator.registerTrait(marker, trait, null);
    }

    /**
     * The same as {@link #registerTrait(Class, Class)} however, only effective server side (including integrated server).
     *
     * @param marker The part marker class.
     * @param trait  The trait to implement.
     */
    public void registerServerTrait(Class<?> marker, Class<? extends TileMultipart> trait) {
        generator.registerTrait(marker, null, trait);
    }

    /**
     * Register the specified class, when found on a {@link MultiPart} instance:<br/>
     * - Implemented the interface on the {@link TileMultipart} instance with all methods proxied through to your part.<br/>
     * - Only allow one instance of a part with this interface in the block space.
     *
     * @param iFace The interface to register.
     */
    public void registerPassthroughInterface(Class<?> iFace) {
        generator.registerPassThroughInterface(iFace);
    }

    /**
     * The same as {@link #registerPassthroughInterface(Class)} however, only effective client side.
     *
     * @param iFace The interface to register.
     */
    public void registerClientPassthroughInterface(Class<?> iFace) {
        generator.registerPassThroughInterface(iFace, true, false);
    }

    /**
     * The same as {@link #registerPassthroughInterface(Class)} however, only effective server side (including integrated server).
     *
     * @param iFace The interface to register.
     */
    public void registerServerPassthroughInterface(Class<?> iFace) {
        generator.registerPassThroughInterface(iFace, false, true);
    }
}
