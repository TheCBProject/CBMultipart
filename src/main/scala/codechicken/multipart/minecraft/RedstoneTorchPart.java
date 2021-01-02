package codechicken.multipart.minecraft;

import codechicken.multipart.api.MultiPartType;
import codechicken.multipart.api.RedstoneInteractions;
import codechicken.multipart.api.part.TRandomTickPart;
import codechicken.multipart.api.part.redstone.IFaceRedstonePart;
import codechicken.multipart.util.TickScheduler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class RedstoneTorchPart extends TorchPart implements IFaceRedstonePart, TRandomTickPart {

    private BurnoutEntry burnout;

    public RedstoneTorchPart() {
    }

    public RedstoneTorchPart(BlockState state) {
        super(state);
    }

    @Override
    public MultiPartType<?> getType() {
        return ModContent.redstoneTorchPartType;
    }

    @Override
    protected Block getStandingBlock() {
        return Blocks.REDSTONE_TORCH;
    }

    @Override
    protected Block getWallBlock() {
        return Blocks.REDSTONE_WALL_TORCH;
    }

    public boolean active() {
        return state.get(RedstoneTorchBlock.LIT);
    }

    @Override
    public void animateTick(Random random) {
        if (!active()) {
            return;
        }

        super.animateTick(random);
    }

    @Override
    public void onNeighborBlockChanged(BlockPos from) {
        if (!world().isRemote) {
            if (!dropIfCantStay() && isBeingPowered() == active()) {
                scheduleTick(2);
            }
        }
    }

    public boolean isBeingPowered() {
        return RedstoneInteractions.getPowerTo(this, getSide().ordinal()) > 0;
    }

    @Override
    public void scheduledTick() {
        if (!world().isRemote && isBeingPowered() == active()) {
            toggle();
        }
    }

    @Override
    public void randomTick() {
        scheduledTick();
    }

    @Override
    public void onWorldJoin() {
        TickScheduler.loadRandomTick(this);
    }

    private boolean burnedOut(boolean add) {
        long time = world().getGameTime();
        while (burnout != null && burnout.timeout <= time) {
            burnout = burnout.next;
        }

        if (add) {
            BurnoutEntry e = new BurnoutEntry(world().getGameTime() + 60);
            if (burnout == null) {
                burnout = e;
            } else {
                BurnoutEntry b = burnout;
                while (b.next != null) {
                    b = b.next;
                }
                b.next = e;
            }
        }

        if (burnout == null) {
            return false;
        }

        int i = 0;
        BurnoutEntry b = burnout;
        while (b != null) {
            i++;
            b = b.next;
        }
        return i >= 8;
    }

    private void toggle() {
        if (active()) {
            if (burnedOut(true)) {
                world().playEvent(1502, pos(), 0);
            }
        } else if (burnedOut(false)) {
            return;
        }

        state = state.with(RedstoneTorchBlock.LIT, !active());

        sendUpdate(this::writeDesc);
        tile().markDirty();
        tile().notifyPartChange(this);
        tile().notifyNeighborChange(1);
    }

    @Override
    public void onRemoved() {
        if (active()) {
            tile().notifyNeighborChange(1);
        }
    }

    @Override
    public void onAdded() {
        super.onAdded();
        if (active()) {
            tile().notifyNeighborChange(1);
        }
        onNeighborBlockChanged(pos());
    }

    @Override
    public int strongPowerLevel(int side) {
        return side == 1 && active() ? 15 : 0;
    }

    @Override
    public int weakPowerLevel(int side) {
        return active() && side != getSide().ordinal() ? 15 : 0;
    }

    @Override
    public boolean canConnectRedstone(int side) {
        return true;
    }

    @Override
    public int getFace() {
        return getSide().ordinal();
    }

    public static class BurnoutEntry {

        public BurnoutEntry(long l) {
            timeout = l;
        }

        long timeout;
        BurnoutEntry next;
    }
}
