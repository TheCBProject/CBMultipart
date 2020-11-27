package codechicken.multipart.minecraft;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.multipart.api.part.IModelRenderPart;
import codechicken.multipart.api.part.NormalOcclusionTest;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.TNormalOcclusionPart;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import java.util.Collections;

public abstract class McStatePart extends TMultiPart implements TNormalOcclusionPart, IModelRenderPart {

    public BlockState state;

    public McStatePart() {
        this.state = getDefaultState();
    }

    public McStatePart(BlockState state) {
        this.state = state;
    }

    public abstract BlockState getDefaultState();

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
        state = getDefaultState().getBlock().getStateForPlacement(context);
        return this;
    }

    @Override
    public float getStrength(PlayerEntity player, PartRayTraceResult hit) {
        return state.getPlayerRelativeBlockHardness(player, player.world, new BlockPos(0, -1, 0));
    }

    @Override
    public int getLightValue() {
        return state.getLightValue();
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
    public VoxelShape getCullingShape() {
        return state.getRenderShape(world(), pos());
    }

    @Override
    public VoxelShape getRayTraceShape() {
        return state.getRaytraceShape(world(), pos());
    }

    @Override
    public VoxelShape getOcclusionShape() {
        return state.getCollisionShape(null, null);
    }

    @Override
    public boolean occlusionTest(TMultiPart npart) {
        return NormalOcclusionTest.apply(this, npart);
    }

    @Override
    public SoundType getPlacementSound(ItemStack stack, PlayerEntity player) {
        return state.getSoundType(world(), pos(), player);
    }

    //    @Override
    //    public Cuboid6 getBounds() {
    //        if (tile() != null) {
    //            //TODO VoxelShape.
    //            return Cuboid6.full;//new Cuboid6(getBlock().getBoundingBox(state, world(), pos()));
    //        } else {
    //            return Cuboid6.full;//new Cuboid6(getBlock().getBoundingBox(state, null, null));
    //        }
    //    }
    //
    //    @Override
    //    @OnlyIn (Dist.CLIENT)
    //    public TextureAtlasSprite getBreakingIcon(CuboidRayTraceResult hit) {
    //        return getBrokenIcon(hit.getFace().ordinal());
    //    }
    //
    //    @Override
    //    @OnlyIn (Dist.CLIENT)
    //    public TextureAtlasSprite getBrokenIcon(int side) {
    //        return TextureUtils.getParticleIconForBlock(state);
    //    }
    //
    //    @Override
    //    public void addHitEffects(CuboidRayTraceResult hit, ParticleManager manager) {
    //        IconHitEffects.addHitEffects(this, hit, manager);
    //    }
    //
    //    @Override
    //    public void addDestroyEffects(CuboidRayTraceResult hit, ParticleManager manager) {
    //        IconHitEffects.addDestroyEffects(this, manager, false);
    //    }
}
