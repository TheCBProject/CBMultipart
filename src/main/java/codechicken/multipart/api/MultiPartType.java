package codechicken.multipart.api;

import codechicken.lib.data.MCDataInput;
import codechicken.multipart.api.part.TMultiPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 3/16/20.
 */
public abstract class MultiPartType<T extends TMultiPart> extends ForgeRegistryEntry<MultiPartType<?>> {

    // Internal.
    @Nullable
    Object renderer;

    public MultiPartType() {
    }

    /**
     * Called to create a {@link TMultiPart} instance on the server
     * side from a {@link CompoundTag} tag. This is called when
     * the MultiPart is loaded from disk.
     *
     * @param tag The {@link CompoundTag} to load from.
     * @return The {@link TMultiPart} instance, or {@code null} to
     * discard.
     */
    @Nullable
    public abstract T createPartServer(CompoundTag tag);

    /**
     * Called to create a {@link TMultiPart} instance from
     * the provided {@link MCDataInput}.
     * <p>
     * The supplied packet comes from {@link TMultiPart#writeDesc}
     *
     * @param packet The packet.
     * @return The client-side part.
     */
    public abstract T createPartClient(MCDataInput packet);
}
