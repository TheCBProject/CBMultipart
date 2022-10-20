package codechicken.multipart.api;

import codechicken.lib.data.MCDataInput;
import codechicken.multipart.api.part.TMultiPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by covers1624 on 3/16/20.
 */
public abstract class MultiPartType<T extends TMultiPart> extends ForgeRegistryEntry<MultiPartType<?>> {

    // Internal.
    Object renderer;

    public MultiPartType() {
    }

    /**
     * Called to create a {@link TMultiPart} instance on the server
     * side from a {@link CompoundTag} tag. This is called when
     * the MultiPart is loaded from disk.
     *
     * @param tag The {@link CompoundTag} to load from.
     * @return The {@link TMultiPart} instance, or null to
     * discard.
     */
    @Nullable
    public abstract T createPartServer(CompoundTag tag);

    /**
     * Called to create a {@link TMultiPart} instance from
     *
     * @param packet
     * @return
     */
    @Nonnull
    public abstract T createPartClient(MCDataInput packet);
}
