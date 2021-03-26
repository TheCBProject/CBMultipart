package codechicken.multipart.minecraft;

import codechicken.lib.data.MCDataInput;
import codechicken.multipart.api.MultiPartType;
import codechicken.multipart.api.part.redstone.IFaceRedstonePart;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;

public class LeverPart extends McSidedStatePart implements IFaceRedstonePart {

    public LeverPart() {
    }

    public LeverPart(BlockState state) {
        super(state);
    }

    @Override
    public MultiPartType<?> getType() {
        return ModContent.leverPartType;
    }

    @Override
    public BlockState defaultBlockState() {
        return Blocks.LEVER.defaultBlockState();
    }

    @Override
    public ItemStack getDropStack() {
        return new ItemStack(Blocks.LEVER);
    }

    public boolean active() {
        return state.getValue(LeverBlock.POWERED);
    }

    @Override
    public Direction getSide() {
        return HorizontalFaceBlock.getConnectedDirection(state).getOpposite();
    }

    @Override
    public ActionResultType activate(PlayerEntity player, PartRayTraceResult hit, ItemStack item, Hand hand) {
        if (world().isClientSide) {
            return ActionResultType.SUCCESS;
        }

        state = state.cycle(LeverBlock.POWERED);
        world().playSound(null, pos(), SoundEvents.LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, active() ? 0.6F : 0.5F);

        sendUpdate(this::writeDesc);
        tile().setChanged();
        tile().notifyPartChange(this);
        tile().notifyNeighborChange(getSide().ordinal());
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onRemoved() {
        if (active()) {
            tile().notifyNeighborChange(getSide().ordinal());
        }
    }

    @Override
    public void onConverted() {
        if (active()) {
            tile().notifyNeighborChange(getSide().ordinal());
        }
    }

    @Override
    public int weakPowerLevel(int side) {
        return active() ? 15 : 0;
    }

    @Override
    public int strongPowerLevel(int side) {
        return active() && side == getSide().ordinal() ? 15 : 0;
    }

    @Override
    public boolean canConnectRedstone(int side) {
        return true;
    }

    @Override
    public int getFace() {
        return getSide().ordinal();
    }

    @Override
    public void readUpdate(MCDataInput packet) {
        super.readUpdate(packet);
        if (active()) {
            LeverBlock.makeParticle(state, world(), pos(), 1.0F);
        }
    }
}
