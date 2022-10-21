package codechicken.microblock.api;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Created by covers1624 on 26/6/22.
 */
public interface MicroMaterial extends IForgeRegistryEntry<MicroMaterial> {

    boolean canRenderInLayer(RenderType layer);

    @OnlyIn (Dist.CLIENT)
    RenderType getItemRenderLayer();

    /**
     * @return If this material is not opaque. (Glass, Ice, etc.)
     */
    boolean isTransparent();

    /**
     * Gets the light level emitted by this micro material.
     *
     * @return The light emission.
     */
    int getLightEmission();

    /**
     * Gets the Strength of this material when being broken
     * by the given player.
     *
     * @param player The player.
     * @return The strength value.
     */
    float getStrength(Player player);

    /**
     * Gets the localized name for this material.
     *
     * @return The localized name.
     */
    Component getLocalizedName();

    /**
     * Gets {@link ItemStack} this material can be cut from.
     *
     * @return The {@link ItemStack}.
     */
    ItemStack getItem();

    /**
     * Get the required saw cutting strength for this material.
     *
     * @return The required cutting strength.
     */
    int getCutterStrength();

    /**
     * Get the breaking/waking sound.
     *
     * @return The {@link SoundType}.
     */
    SoundType getSound();

    /**
     * Get the resistance of this material for the given explosion.
     *
     * @param level     The level.
     * @param pos       The position.
     * @param explosion The explosion.
     * @return The resistance.
     */
    float getExplosionResistance(BlockGetter level, BlockPos pos, Explosion explosion);
}
