//package codechicken.multipart.minecraft;
//
//import codechicken.multipart.IFaceRedstonePart;
//import codechicken.multipart.RedstoneInteractions;
//import codechicken.multipart.TRandomUpdateTickPart;
//import codechicken.multipart.TickScheduler;
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.Blocks;
//import net.minecraft.block.RedstoneTorchBlock;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.SoundCategory;
//import net.minecraft.util.SoundEvents;
//import net.minecraft.util.math.BlockPos;
//
//import java.util.Random;
//
//public class RedstoneTorchPart extends TorchPart implements IFaceRedstonePart, TRandomUpdateTickPart {
//
//    public static RedstoneTorchBlock torchBlock = (RedstoneTorchBlock) Blocks.REDSTONE_TORCH;
//
//    public class BurnoutEntry {
//
//        public BurnoutEntry(long l) {
//            timeout = l;
//        }
//
//        long timeout;
//        BurnoutEntry next;
//    }
//
//    private BurnoutEntry burnout;
//
//    public RedstoneTorchPart() {
//        state = torchBlock.getDefaultState();
//    }
//
//    public RedstoneTorchPart(BlockState state) {
//        super(state);
//    }
//
//    @Override
//    public ResourceLocation getType() {
//        return Content.REDTORCH;
//    }
//
//    @Override
//    public Block getBlock() {
//        return torchBlock;
//    }
//
//    public boolean active() {
//        return state.getBlock() == torchBlock;
//    }
//
//    @Override
//    public void randomDisplayTick(Random random) {
//        if (!active()) {
//            return;
//        }
//
//        super.randomDisplayTick(random);
//    }
//
//    @Override
//    public ItemStack getDropStack() {
//        return new ItemStack(torchBlock);
//    }
//
//    @Override
//    public void onNeighborBlockChanged(BlockPos from) {
//        if (!world().isRemote) {
//            if (!dropIfCantStay() && isBeingPowered() == active()) {
//                scheduleTick(2);
//            }
//        }
//    }
//
//    public boolean isBeingPowered() {
//        return RedstoneInteractions.getPowerTo(this, getSideFromState().ordinal()) > 0;
//    }
//
//    @Override
//    public void scheduledTick() {
//        if (!world().isRemote && isBeingPowered() == active()) {
//            toggle();
//        }
//    }
//
//    @Override
//    public void randomUpdate() {
//        scheduledTick();
//    }
//
//    @Override
//    public void onWorldJoin() {
//        TickScheduler.loadRandomTick(this);
//    }
//
//    private boolean burnedOut(boolean add) {
//        long time = world().getGameTime();
//        while (burnout != null && burnout.timeout <= time) {
//            burnout = burnout.next;
//        }
//
//        if (add) {
//            BurnoutEntry e = new BurnoutEntry(world().getGameTime() + 60);
//            if (burnout == null) {
//                burnout = e;
//            } else {
//                BurnoutEntry b = burnout;
//                while (b.next != null) {
//                    b = b.next;
//                }
//                b.next = e;
//            }
//        }
//
//        if (burnout == null) {
//            return false;
//        }
//
//        int i = 0;
//        BurnoutEntry b = burnout;
//        while (b != null) {
//            i++;
//            b = b.next;
//        }
//        return i >= 8;
//    }
//
//    private void toggle() {
//        if (active()) {
//            if (burnedOut(true)) {
//                world().playSound(null, pos(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5F, 2.6F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.8F);
//                McMultipartSPH.spawnBurnoutSmoke(world(), pos());
//            }
//        } else if (burnedOut(false)) {
//            return;
//        }
//
//        //        if (active()) {
//        //            state = torchBlock.getDefaultState().with(BlockRedstoneTorch.FACING, state.getValue(BlockRedstoneTorch.FACING));
//        //        } else {
//        //            state = torchBlock.getDefaultState().withProperty(BlockRedstoneTorch.FACING, state.getValue(BlockRedstoneTorch.FACING));
//        //        }
//
//        sendDescUpdate();
//        tile().markDirty();
//        tile().notifyPartChange(this);
//        tile().notifyNeighborChange(1);
//    }
//
//    @Override
//    public void onRemoved() {
//        if (active()) {
//            tile().notifyNeighborChange(1);
//        }
//    }
//
//    @Override
//    public void onAdded() {
//        if (active()) {
//            tile().notifyNeighborChange(1);
//        }
//        onNeighborBlockChanged(pos());
//    }
//
//    @Override
//    public int strongPowerLevel(int side) {
//        return side == 1 && active() ? 15 : 0;
//    }
//
//    @Override
//    public int weakPowerLevel(int side) {
//        return active() && side != getSideFromState().ordinal() ? 15 : 0;
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
