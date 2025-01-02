package codechicken.microblock.api;

import codechicken.microblock.CBMicroblock;
import codechicken.microblock.util.MicroMaterialRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Created by covers1624 on 26/6/22.
 */
public abstract class MicroMaterial {

    /**
     * The registry name used by MicroMaterial.
     */
    public static final ResourceKey<Registry<MicroMaterial>> MULTIPART_TYPES = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(CBMicroblock.MOD_ID, "micro_material")
    );
    public static final Registry<MicroMaterial> REGISTRY = new RegistryBuilder<>(MULTIPART_TYPES)
            .sync(true)
            .create();

    public static final Codec<MicroMaterial> CODEC = REGISTRY.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, MicroMaterial> STREAM_CODEC = ByteBufCodecs.registry(MULTIPART_TYPES);

    @Nullable
    Object renderProperties;

    public MicroMaterial() {
        initClient();
    }

    /**
     * @return Key this material is registered under
     */
    public ResourceLocation getRegistryName() {
        return Objects.requireNonNull(MicroMaterialRegistry.microMaterials().getKey(this));
    }

    /**
     * @return If this material is not opaque. (Glass, Ice, etc.)
     */
    public abstract boolean isTransparent();

    /**
     * Gets the light level emitted by this micro material.
     *
     * @return The light emission.
     */
    public abstract int getLightEmission();

    /**
     * Gets the Strength of this material when being broken
     * by the given player.
     *
     * @param player The player.
     * @return The strength value.
     */
    public abstract float getStrength(Player player);

    /**
     * Gets the localized name for this material.
     *
     * @return The localized name.
     */
    public abstract Component getLocalizedName();

    /**
     * Gets {@link ItemStack} this material can be cut from.
     *
     * @return The {@link ItemStack}.
     */
    public abstract ItemStack getItem();

    /**
     * Get the breaking/waking sound.
     *
     * @return The {@link SoundType}.
     */
    @Nullable
    public abstract SoundType getSound();

    /**
     * Get the resistance of this material for the given explosion.
     *
     * @param level     The level.
     * @param pos       The position.
     * @param explosion The explosion.
     * @return The resistance.
     */
    public abstract float getExplosionResistance(BlockGetter level, BlockPos pos, Explosion explosion);

    /**
     * Initialize any client-side properties for this MicroMaterial.
     *
     * @param cons Consumer to set this material's {@link MicroMaterialClient}.
     */
    public void initializeClient(Consumer<MicroMaterialClient> cons) {
    }

    private void initClient() {
        if (FMLEnvironment.dist.isClient() && !DatagenModLoader.isRunningDataGen()) {
            initializeClient(props -> renderProperties = props);
        }
    }
}
