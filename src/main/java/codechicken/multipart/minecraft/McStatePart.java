package codechicken.multipart.minecraft;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.api.NormalOcclusionTest;
import codechicken.multipart.api.part.*;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public abstract class McStatePart extends BaseMultipart implements NormalOcclusionPart, ModelRenderPart, IconHitEffectsPart {

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
    public Iterable<ItemStack> getDrops() {
        return Collections.singletonList(getDropStack());
    }

    @Override
    public ItemStack getCloneStack(PartRayTraceResult hit) {
        return getDropStack();
    }

    @Override
    public void save(CompoundTag tag) {
        tag.put("state", NbtUtils.writeBlockState(state));
    }

    @Override
    public void load(CompoundTag tag) {
        //TODO is this right?
        state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("state"));
    }

    @Override
    public void writeDesc(MCDataOutput packet) {
        //TODO, Read/Write BlockState?
        packet.writeCompoundNBT(NbtUtils.writeBlockState(state));
    }

    @Override
    public void readDesc(MCDataInput packet) {
        state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), packet.readCompoundNBT());
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
        return state.getOcclusionShape(level(), pos());
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
    public SoundType getPlacementSound(UseOnContext context) {
        return state.getSoundType(level(), pos(), context.getPlayer());
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
                .getParticleIcon(getModelData());
    }
}
