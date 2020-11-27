//package codechicken.multipart.minecraft;
//
//import codechicken.lib.raytracer.CuboidRayTraceResult;
//import codechicken.lib.vec.Cuboid6;
//import codechicken.lib.vec.Rotation;
//import codechicken.lib.vec.Vector3;
//import codechicken.multipart.IFaceRedstonePart;
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.Blocks;
//import net.minecraft.block.LeverBlock;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.*;
//
//public class LeverPart extends McSidedMetaPart implements IFaceRedstonePart {
//
//    public static LeverBlock lever = (LeverBlock) Blocks.LEVER;
//
//    public static Cuboid6[][] bounds = new Cuboid6[6][2];
//
//    static {
//        //Because vanilla bounds are wierd and dont allow nice multipart fitting
//        bounds[0][0] = new Cuboid6(5 / 16D, 0 / 16D, 3 / 16D, 11 / 16D, 6 / 16D, 13 / 16D);
//        for (int r = 0; r < 2; r++) {
//            for (int s = 0; s < 6; s++) {
//                bounds[s][r] = bounds[0][0].copy().apply(Rotation.sideOrientation(s, r).at(Vector3.center));
//            }
//        }
//    }
//
//    public LeverPart() {
//        state = lever.getDefaultState();
//    }
//
//    public LeverPart(BlockState state) {
//        super(state);
//    }
//
//    @Override
//    public Block getBlock() {
//        return lever;
//    }
//
//    @Override
//    public ResourceLocation getType() {
//        return Content.LEVER;
//    }
//
//    public boolean active() {
//        return state.get(LeverBlock.POWERED);
//    }
//
//    @Override
//    public Direction getSideFromState() {
//        return state.get(LeverBlock.HORIZONTAL_FACING).getOpposite();
//    }
//
//    @Override
//    public boolean activate(PlayerEntity player, CuboidRayTraceResult hit, ItemStack item, Hand hand) {
//        if (world().isRemote) {
//            return true;
//        }
//
//        state = state.cycle(LeverBlock.POWERED);
//        world().playSound(null, pos(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, active() ? 0.6F : 0.5F);
//
//        sendDescUpdate();
//        tile().markDirty();
//        tile().notifyPartChange(this);
//        tile().notifyNeighborChange(getSideFromState().ordinal());
//        return true;
//    }
//
//    @Override
//    public void onRemoved() {
//        if (active()) {
//            tile().notifyNeighborChange(getSideFromState().ordinal());
//        }
//    }
//
//    @Override
//    public void onConverted() {
//        if (active()) {
//            tile().notifyNeighborChange(getSideFromState().ordinal());
//        }
//    }
//
//    @Override
//    public Cuboid6 getBounds() {
//        //TODO
////        LeverBlock.EnumOrientation facing = state.getValue(BlockLever.FACING);
////        int r = facing == BlockLever.EnumOrientation.DOWN_X || facing == BlockLever.EnumOrientation.UP_X ? 1 : 0;
////
////        return bounds[getSideFromState()][r];
//        return Cuboid6.full;
//    }
//
//    @Override
//    public int weakPowerLevel(int side) {
//        return active() ? 15 : 0;
//    }
//
//    @Override
//    public int strongPowerLevel(int side) {
//        return active() && side == getSideFromState().ordinal() ? 15 : 0;
//    }
//
//    @Override
//    public boolean canConnectRedstone(int side) {
//        return true;
//    }
//
//    @Override
//    public int getFace() {
//        return getSideFromState().ordinal();
//    }
//}
