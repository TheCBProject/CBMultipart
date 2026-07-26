package codechicken.multipart.minecraft;

import codechicken.lib.packet.CCStreamCodecs;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.api.NormalOcclusionTest;
import codechicken.multipart.api.part.*;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public abstract class McStatePart extends BaseMultipart implements NormalOcclusionPart, BlockStateModelPart, IconHitEffectsPart {

    public BlockState state;

    public McStatePart() {
        this.state = defaultBlockState();
    }

    public McStatePart(BlockState state) {
        this.state = state;
    }

    public abstract BlockState defaultBlockState();

    public abstract ItemStack getDropStack();

    @Override
    public Iterable<ItemStack> getDrops(LootParams.Builder builder) {
        return Collections.singletonList(getDropStack());
    }

    @Override
    public ItemStack getCloneStack(PartRayTraceResult hit, Player player) {
        return getDropStack();
    }

    @Override
    public void save(ValueOutput output) {
        output.store("state", BlockState.CODEC, state);
    }

    @Override
    public void load(ValueInput input) {
        state = input.read("state", BlockState.CODEC).orElseGet(this::defaultBlockState);
    }

    @Override
    public void writeDesc(RegistryFriendlyByteBuf packet) {
        packet.cc$writeWithCodec(CCStreamCodecs.BLOCK_STATE, state);
    }

    @Override
    public void readDesc(RegistryFriendlyByteBuf packet) {
        state = packet.cc$readWithCodec(CCStreamCodecs.BLOCK_STATE);
    }

    @Override
    public BlockState getCurrentState() {
        return state;
    }

    @Nullable
    public MultiPart setStateOnPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().getBlock().getStateForPlacement(context);
        if (state != null) {
            this.state = state;
            return this;
        }
        return null;
    }

    @Override
    public float getStrength(Player player, PartRayTraceResult hit) {
        return state.getDestroyProgress(player, player.level(), new BlockPos(0, -1, 0));
    }

    @Override
    public int getLightEmission() {
        return state.getLightEmission();
    }

    @Override
    public VoxelShape getShape(CollisionContext context) {
        return state.getShape(level(), pos(), context);
    }

    @Override
    public VoxelShape getCollisionShape(CollisionContext context) {
        return state.getCollisionShape(level(), pos(), context);
    }

    @Override
    public VoxelShape getRenderOcclusionShape() {
        return state.getOcclusionShape();
    }

    @Override
    public VoxelShape getInteractionShape() {
        return state.getInteractionShape(level(), pos());
    }

    @Override
    public VoxelShape getOcclusionShape() {
        VoxelShape cShape = state.getCollisionShape(null, null);
        return cShape.isEmpty() ? state.getShape(null, null) : cShape;
    }

    @Override
    public VoxelShape getBlockSupportShape() {
        return state.getBlockSupportShape(level(), pos());
    }

    @Override
    public VoxelShape getVisualShape(CollisionContext context) {
        return state.getVisualShape(level(), pos(), context);
    }

    @Override
    public boolean occlusionTest(MultiPart nPart) {
        return NormalOcclusionTest.test(this, nPart);
    }

    @Override
    public SoundType getSound(@Nullable UseOnContext useOnContext) {
        return state.getSoundType(level(), pos(), useOnContext != null ? useOnContext.getPlayer() : null);
    }

    @Override
    public Cuboid6 getBounds() {
        return new Cuboid6(getShape(CollisionContext.empty()).bounds());
    }

    @Override
    @OnlyIn (Dist.CLIENT)
    public TextureAtlasSprite getBreakingIcon(PartRayTraceResult hit) {
        return getBrokenIcon(hit.getDirection().ordinal());
    }

    @Override
    @OnlyIn (Dist.CLIENT)
    public TextureAtlasSprite getBrokenIcon(int side) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
                .getBlockModel(getCurrentState())
                .particleIcon(); // TODO fake level for model data
    }
}
