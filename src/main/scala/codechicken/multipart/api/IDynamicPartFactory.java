package codechicken.multipart.api;

import codechicken.lib.data.MCDataInput;
import codechicken.multipart.TMultiPart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/**
 * A version of IPartFactory that can construct parts based on the data that is to be loaded to it (NBT for servers, packets for clients).
 * This is used in cases where the class of the part can change depending on the data it will be given.
 */
public interface IDynamicPartFactory {

	/**
	 * Create a new server instance of the part with the specified identifier.
	 * As there is no guarantee on the passed in tag being non-null or on what it contains,
	 * You need to be able to safely handle invalid/null tags.
	 *
	 * @param identifier The identifier of the part to create.
	 * @param compound   The tag to pass to {@link TMultiPart#load(NBTTagCompound)}
	 * @return The new part, null to delete the part.
	 */
	TMultiPart createPartServer(ResourceLocation identifier, NBTTagCompound compound);

	/**
	 * Create a new server instance of the part with the specified identifier.
	 * As there is no guarantee on the passed in packet being non-null or on what it contains,
	 * You need to be able to safely handle invalid/null packet.
	 *
	 * @param identifier The identifier of the part to create.
	 * @param packet     The tag to pass to {@link TMultiPart#readDesc(MCDataInput)}
	 * @return The new part, null to delete the part.
	 */
	TMultiPart createPartClient(ResourceLocation identifier, MCDataInput packet);

}
