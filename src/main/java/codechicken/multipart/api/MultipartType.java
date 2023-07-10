package codechicken.multipart.api;

import codechicken.lib.data.MCDataInput;
import codechicken.multipart.CBMultipart;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.init.MultiPartRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Created by covers1624 on 3/16/20.
 */
public abstract class MultipartType<T extends MultiPart> {

    /**
     * The Forge registry name used by MultipartType.
     */
    public static final ResourceLocation MULTIPART_TYPES = new ResourceLocation(CBMultipart.MOD_ID, "multipart_types");

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

    public ResourceLocation getRegistryName() {
        return Objects.requireNonNull(MultiPartRegistries.MULTIPART_TYPES.getKey(this));
    }
}
