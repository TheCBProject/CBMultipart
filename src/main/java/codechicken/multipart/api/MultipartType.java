package codechicken.multipart.api;

import codechicken.multipart.CBMultipart;
import codechicken.multipart.api.part.MultiPart;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.registries.RegistryBuilder;
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
    public static final Registry<MultipartType<?>> REGISTRY = new RegistryBuilder<>(MULTIPART_TYPES)
            .sync(true)
            .create();
    public static final Codec<MultipartType<?>> TYPE_CODEC = REGISTRY.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, MultipartType<?>> STREAM_CODEC = ByteBufCodecs.registry(MULTIPART_TYPES);

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
     * the provided {@link RegistryFriendlyByteBuf}.
     * <p>
     * The supplied packet comes from {@link MultiPart#writeDesc}
     *
     * @param packet The packet.
     * @return The client-side part.
     */
    public abstract T createPartClient(RegistryFriendlyByteBuf packet);

    public Identifier getRegistryName() {
        return Objects.requireNonNull(REGISTRY.getKey(this));
    }

    // Internal arms-length client-only fields.
    @Nullable
    Object dynamicRenderer;
    @Nullable
    Object dynamicRendererFactory;

    @Nullable
    Object staticRenderer;
    @Nullable
    Object staticRendererFactory;

    @Nullable
    Object outlineRenderer;
}
