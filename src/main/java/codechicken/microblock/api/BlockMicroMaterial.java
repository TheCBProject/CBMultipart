package codechicken.microblock.api;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.particle.CustomBreakingParticle;
import codechicken.lib.render.particle.CustomParticleHandler;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.client.MicroblockRender;
import codechicken.microblock.part.MicroblockPart;
import codechicken.microblock.util.MaskedCuboid;
import codechicken.multipart.util.PartRayTraceResult;
import net.covers1624.quack.collection.FastStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

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
    public void initializeClient(Consumer<MicroMaterialClient> cons) {
        cons.accept(new MicroMaterialClient() {

            @Override
            public RenderType getItemRenderLayer() {
                return ItemBlockRenderTypes.getRenderType(state, true);
            }

            @Override
            public List<BakedQuad> getQuads(MicroblockPart part, @Nullable Direction side, @Nullable RenderType layer, Iterable<MaskedCuboid> cuboids) {
                if (side != null) return List.of();

                return MicroblockRender.getQuads(part, state, layer, cuboids);
            }

            @Override
            public void renderCuboids(CCRenderState ccrs, @Nullable RenderType layer, Iterable<MaskedCuboid> cuboids) {
                MicroblockRender.renderCuboids(ccrs, state, layer, cuboids);
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
                Level level = part.level();
                ParticleEngine manager = Minecraft.getInstance().particleEngine;
                TextureAtlasSprite sprite = getSprite(level, part.pos());

                if (numberOfParticles != 0) {
                    for (int i = 0; i < numberOfParticles; i++) {
                        double mX = level.random.nextGaussian() * 0.15F;
                        double mY = level.random.nextGaussian() * 0.15F;
                        double mZ = level.random.nextGaussian() * 0.15F;
                        manager.add(CustomBreakingParticle.newLandingParticle((ClientLevel) level, entity.x, entity.y, entity.z, mX, mY, mZ, sprite));
                    }
                }
            }

            @Override
            public void addRunningEffects(MicroblockPart part, PartRayTraceResult hit, Entity entity) {
                Level level = part.level();
                ParticleEngine manager = Minecraft.getInstance().particleEngine;
                TextureAtlasSprite sprite = getSprite(level, part.pos());

                double x = entity.getX() + (level.random.nextFloat() - 0.5D) * entity.getBbWidth();
                double y = entity.getBoundingBox().minY + 0.1D;
                double z = entity.getZ() + (level.random.nextFloat() - 0.5D) * entity.getBbWidth();
                manager.add(new CustomBreakingParticle((ClientLevel) level, x, y, z, -entity.getDeltaMovement().x * 4.0D, 1.5D, -entity.getDeltaMovement().z * 4.0D, sprite));
            }

            private TextureAtlasSprite getSprite(Level level, BlockPos pos) {
                return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getTexture(state, level, pos);
            }
        });
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
        ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(block);
        StringBuilder path = new StringBuilder(blockKey.getPath());
        if (!state.getProperties().isEmpty()) {
            path.append("//");

            record ValuePair(Property<?> key, Comparable<?> value) { }
            // Stable sort all keys based off their name, otherwise they may differ on the server/client.
            var entries = FastStream.of(state.getValues().entrySet())
                    .sorted(Comparator.comparing(e -> e.getKey().getName()))
                    .map(e -> new ValuePair(e.getKey(), e.getValue()))
                    .toList();
            for (var entry : entries) {
                Property<?> property = entry.key;
                if (path.charAt(path.length() - 2) != '/') {
                    path.append('/');
                }
                path.append(property.getName()).append('.').append(property.getName(unsafeCast(entry.value)));
            }
        }
        return ResourceLocation.fromNamespaceAndPath(blockKey.getNamespace(), path.toString());
    }
}
