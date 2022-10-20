package codechicken.microblock.api;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistryEntry.UncheckedRegistryEntry;

import java.util.Map;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 26/6/22.
 */
public class BlockMicroMaterial extends UncheckedRegistryEntry<MicroMaterial> implements MicroMaterial {

    public final BlockState state;

    public BlockMicroMaterial(Block block) {
        this(block.defaultBlockState());
    }

    public BlockMicroMaterial(BlockState state) {
        this.state = state;
        setRegistryName();
    }

    protected void setRegistryName() {
        setRegistryName(makeMaterialKey(state));
    }

    @Override
    public boolean canRenderInLayer(RenderType layer) {
        return ItemBlockRenderTypes.canRenderInLayer(state, layer);
    }

    @Override
    public RenderType getItemRenderLayer() {
        return ItemBlockRenderTypes.getRenderType(state, true);
    }

    @Override
    public int getLightEmission() {
        return state.getLightEmission();
    }

    @Override
    public float getStrength(Player player) {
        return state.getDestroyProgress(player, player.level, new BlockPos(0, -1, 0));
    }

    @Override
    public Component getLocalizedName() {
        return getItem().getHoverName();
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(state.getBlock(), 1);
    }

    @Override
    public int getCutterStrength() {
        return 0; // TODO harvest levels have changed.
    }

    @Override
    public SoundType getSound() {
        return state.getBlock().getSoundType(state);
    }

    @Override
    public float getExplosionResistance(BlockGetter level, BlockPos pos, Explosion explosion) {
        return state.getExplosionResistance(level, pos, explosion);
    }

    /**
     * Builds a {@link BlockMicroMaterial} registry name for the given {@link BlockState}.
     * <p>
     * Due to the restrictions imposed by {@link ResourceLocation}, the following format is used:<br>
     * {@code mod_id:block_name//property1.value1/property2.value2/property3.value3}
     *
     * @param state The {@link BlockState} to create a name for.
     * @return The name.
     */
    public static ResourceLocation makeMaterialKey(BlockState state) {
        Block block = state.getBlock();
        StringBuilder path = new StringBuilder(block.getRegistryName().getPath());
        if (!state.getProperties().isEmpty()) {
            path.append("//");

            for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
                Property<?> property = entry.getKey();
                if (path.charAt(path.length() - 2) != '/') {
                    path.append('/');
                }
                path.append(property.getName()).append('.').append(property.getName(unsafeCast(entry.getValue())));
            }
        }
        return new ResourceLocation(block.getRegistryName().getNamespace(), path.toString());
    }
}
