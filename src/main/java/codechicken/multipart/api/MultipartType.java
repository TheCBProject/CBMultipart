package codechicken.multipart.api;

import codechicken.lib.data.MCDataInput;
import codechicken.multipart.api.part.MultiPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 3/16/20.
 */
public abstract class MultipartType<T extends MultiPart> extends ForgeRegistryEntry<MultipartType<?>> {

    // Internal.
    @Nullable
    Object renderer;

    public MultipartType() {
    }

    /**
     * Called to create a {@link MultiPart} instance on the server
     * side from a {@link CompoundTag} tag. This is called when
     * the MultiPart is loaded from disk.
     *
     * @param tag The {@link CompoundTag} to load from.
     * @return The {@link MultiPart} instance, or {@code null} to
     * discard.
     */
    @Nullable
    public abstract T createPartServer(CompoundTag tag);

    /**
     * Called to create a {@link MultiPart} instance from
     * the provided {@link MCDataInput}.
     * <p>
     * The supplied packet comes from {@link MultiPart#writeDesc}
     *
     * @param packet The packet.
     * @return The client-side part.
     */
    public abstract T createPartClient(MCDataInput packet);
}
