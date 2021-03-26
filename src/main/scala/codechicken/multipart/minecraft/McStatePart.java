package codechicken.multipart.minecraft;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.api.NormalOcclusionTest;
import codechicken.multipart.api.part.*;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import java.util.Collections;

public abstract class McStatePart extends TMultiPart implements TNormalOcclusionPart, IModelRenderPart, TIconHitEffectsPart {

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
    public ItemStack pickItem(PartRayTraceResult hit) {
        return getDropStack();
    }

    @Override
    public void save(CompoundNBT tag) {
        tag.put("state", NBTUtil.writeBlockState(state));
    }

    @Override
    public void load(CompoundNBT tag) {
        state = NBTUtil.readBlockState(tag.getCompound("state"));
    }

    @Override
    public void writeDesc(MCDataOutput packet) {
        //TODO, Read/Write BlockState?
        packet.writeCompoundNBT(NBTUtil.writeBlockState(state));
    }

    @Override
    public void readDesc(MCDataInput packet) {
        state = NBTUtil.readBlockState(packet.readCompoundNBT());
    }

    @Override
    public boolean canRenderInLayer(RenderType layer) {
        return RenderTypeLookup.canRenderInLayer(state, layer);
    }

    @Override
    public BlockState getCurrentState() {
        return state;
    }

    @Override
    public IModelData getModelData() {
        return EmptyModelData.INSTANCE;
    }

    public TMultiPart setStateOnPlacement(BlockItemUseContext context) {
        state = defaultBlockState().getBlock().getStateForPlacement(context);
        return this;
    }

    @Override
    public float getStrength(PlayerEntity player, PartRayTraceResult hit) {
        return state.getDestroyProgress(player, player.level, new BlockPos(0, -1, 0));
    }

    @Override
    public int getLightValue() {
        return state.getLightEmission();
    }

    @Override
    public VoxelShape getOutlineShape() {
        return state.getShape(world(), pos());
    }

    @Override
    public VoxelShape getCollisionShape() {
        return state.getCollisionShape(world(), pos());
    }

    @Override
    public VoxelShape getRenderOcclusionShape() {
        return state.getOcclusionShape(world(), pos());
    }

    @Override
    public VoxelShape getInteractionShape() {
        return state.getInteractionShape(world(), pos());
    }

    @Override
    public VoxelShape getOcclusionShape() {
        return state.getCollisionShape(null, null);
    }

    @Override
    public boolean occlusionTest(TMultiPart npart) {
        return NormalOcclusionTest.test(this, npart);
    }

    @Override
    public SoundType getPlacementSound(ItemUseContext context) {
        return state.getSoundType(world(), pos(), context.getPlayer());
    }

    //TODO, Temporary.
    @Override
    public boolean renderStatic(RenderType layer, CCRenderState ccrs) {
        return IModelRenderPart.super.renderStatic(layer, ccrs);
    }

    @Override
    public Cuboid6 getBounds() {
        return new Cuboid6(getOutlineShape().bounds());
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
                .getParticleTexture(getModelData());
    }

    @Override
    public void addHitEffects(PartRayTraceResult hit, ParticleManager manager) {
        IconHitEffects.addHitEffects(this, hit, manager);
    }

    @Override
    public void addDestroyEffects(PartRayTraceResult hit, ParticleManager manager) {
        IconHitEffects.addDestroyEffects(this, manager, true);
    }
}
