package codechicken.microblock.api;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistryEntry.UncheckedRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Created by covers1624 on 26/6/22.
 */
public abstract class MicroMaterial extends UncheckedRegistryEntry<MicroMaterial> {

    @Nullable
    Object renderProperties;

    public MicroMaterial() {
        initClient();
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
     * Gets the Tier that is required to cut this material.
     *
     * @return The required tier for cutting. Null specifies max available.
     */
    @Nullable
    public abstract Tier getCutterTier();

    /**
     * Get the breaking/waking sound.
     *
     * @return The {@link SoundType}.
     */
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
        if (FMLEnvironment.dist == Dist.CLIENT && !FMLLoader.getLaunchHandler().isData()) {
            initializeClient(props -> renderProperties = props);
        }
    }
}
