package codechicken.microblock.api;

import codechicken.lib.render.particle.CustomParticleHandler;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.client.MicroblockRender;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.item.SawComponent;
import codechicken.microblock.part.MicroblockPart;
import codechicken.microblock.util.MaskedCuboid;
import codechicken.multipart.util.PartRayTraceResult;
import com.google.common.collect.ImmutableSet;
import net.covers1624.quack.collection.FastStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 26/6/22.
 */
public class BlockMicroMaterial extends MicroMaterial {

    public final BlockState state;

    public BlockMicroMaterial(Block block) {
        this(block.defaultBlockState());
    }

    public BlockMicroMaterial(BlockState state) {
        this.state = state;
    }

    @Override
    public boolean isTransparent() {
        return !state.canOcclude();
    }

    @Override
    public int getLightEmission() {
        return state.getLightEmission();
    }

    @Override
    public float getStrength(Player player) {
        return state.getDestroyProgress(player, player.level(), new BlockPos(0, -1, 0));
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
    public SoundType getSound() {
        return state.getSoundType();
    }

    @Override
    public float getExplosionResistance(BlockGetter level, BlockPos pos, Explosion explosion) {
        return state.getExplosionResistance(level, pos, explosion);
    }

    @Override
    public boolean isCuttableBySaw(ItemStack saw) {
        if (sawCutsEverything(saw)) {
            return true;
        }

        var component = SawComponent.getComponent(saw);
        if (component == null) return false;

        return component.canCut(state);
    }

    private boolean sawCutsEverything(ItemStack saw) {
        return CBMicroblockModContent.netheriteSawCutsEverything && saw.is(CBMicroblockModContent.NETHERITE_SAW);
    }

    @Override
    public void initializeClient(Consumer<MicroMaterialClient> cons) {
        cons.accept(new MicroMaterialClient() {

            @Override
            public void collectParts(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, ImmutableSet<MaskedCuboid> renderCuboids, List<BlockModelPart> parts) {
                MicroblockRender.collectParts(level, pos, state, renderCuboids, parts);
            }

            @Override
            public void addHitEffects(MicroblockPart part, PartRayTraceResult hit, ParticleEngine engine) {
                CustomParticleHandler.addBlockHitEffects(
                        part.level(),
                        part.getBounds().copy().add(part.pos()),
                        hit.getDirection(),
                        getSprite(part.level(), part.pos()),
                        engine
                );
            }

            @Override
            public void addDestroyEffects(MicroblockPart part, PartRayTraceResult hit, ParticleEngine engine) {
                CustomParticleHandler.addBlockDestroyEffects(
                        part.level(),
                        part.getBounds().copy().add(part.pos()),
                        List.of(getSprite(part.level(), part.pos())),
                        engine
                );
            }

            @Override
            public void addLandingEffects(MicroblockPart part, PartRayTraceResult hit, Vector3 entity, int numberOfParticles) {
                CustomParticleHandler.addLandingEffects(
                        part.level(),
                        entity,
                        getSprite(part.level(), part.pos()),
                        numberOfParticles
                );
            }

            @Override
            public void addRunningEffects(MicroblockPart part, PartRayTraceResult hit, Entity entity) {
                CustomParticleHandler.addRunningEffects(
                        part.level(),
                        entity,
                        getSprite(part.level(), part.pos())
                );
            }

            private TextureAtlasSprite getSprite(Level level, BlockPos pos) {
                return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state, level, pos);
            }
        });
    }

    /**
     * Builds a {@link BlockMicroMaterial} registry name for the given {@link BlockState}.
     * <p>
     * Due to the restrictions imposed by {@link Identifier}, the following format is used:<br>
     * {@code mod_id:block_name//property1.value1/property2.value2/property3.value3}
     *
     * @param state The {@link BlockState} to create a name for.
     * @return The name.
     */
    public static Identifier makeMaterialKey(BlockState state) {
        Block block = state.getBlock();
        Identifier blockKey = BuiltInRegistries.BLOCK.getKey(block);
        String path = blockKey.getPath();
        if (!state.getProperties().isEmpty()) {
            // Stable sort all keys based off their name, otherwise they may differ on the server/client.
            path += "//" + FastStream.of(state.getValues().entrySet())
                    .sorted(Comparator.comparing(e -> e.getKey().getName()))
                    .map(e -> e.getKey().getName() + "." + e.getKey().getName(unsafeCast(e.getValue())))
                    .join("/");
        }
        return Identifier.fromNamespaceAndPath(blockKey.getNamespace(), path);
    }
}
