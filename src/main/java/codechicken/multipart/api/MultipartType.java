package codechicken.multipart.api;

import codechicken.lib.data.MCDataInput;
import codechicken.multipart.CBMultipart;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.init.MultiPartRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Created by covers1624 on 3/16/20.
 */
public abstract class MultipartType<T extends MultiPart> {

    /**
     * The registry name used by MultipartType.
     */
    public static final ResourceKey<Registry<MultipartType<?>>> MULTIPART_TYPES = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(CBMultipart.MOD_ID, "multipart_types"));
    // TODO this is cyclic
//    public static final Codec<MultipartType<?>> TYPE_CODEC = MultiPartRegistries.MULTIPART_TYPES.byNameCodec();

    public static Codec<MultipartType<?>> TYPE_CODEC() {
        return MultiPartRegistries.MULTIPART_TYPES.byNameCodec();
    }

    // Internal.
    @Nullable
    Object renderer;

    public MultipartType() {
    }

    /**
     * Called to create a {@link MultiPart} instance on the server
     * side from a {@link ValueInput} tag. This is called when
     * the MultiPart is loaded from disk.
     *
     * @param input The {@link ValueInput} to load from.
     * @return The {@link MultiPart} instance, or {@code null} to
     * discard.
     */
    @Nullable
    public abstract T createPartServer(ValueInput input);

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

    public Identifier getRegistryName() {
        return Objects.requireNonNull(MultiPartRegistries.multipartTypes().getKey(this));
    }
}
