package codechicken.multipart.api;

import codechicken.multipart.TMultiPart;
import net.minecraft.util.ResourceLocation;

/**
 * Interface to be registered for constructing parts.
 * Every instance of every multipart is constructed from an implementor of this.
 */
public interface IPartFactory {

    /**
     * Create a new instance of the part with the specified identifier.
     *
     * @param identifier The identifier of the part to create.
     * @param client     If we are creating the part on the client or the server.
     * @return The part that was created.
     */
    TMultiPart createPart(ResourceLocation identifier, boolean client);

}
