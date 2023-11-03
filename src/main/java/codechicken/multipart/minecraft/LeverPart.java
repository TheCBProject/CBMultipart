package codechicken.multipart.minecraft;

import codechicken.lib.data.MCDataInput;
import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.redstone.FaceRedstonePart;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import org.jetbrains.annotations.Nullable;

public class LeverPart extends McSidedStatePart implements FaceRedstonePart {

    public LeverPart() {
    }

    public LeverPart(BlockState state) {
        super(state);
    }

    @Override
    public MultipartType<?> getType() {
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
        return FaceAttachedHorizontalDirectionalBlock.getConnectedDirection(state).getOpposite();
    }

    @Override
    public @Nullable MultiPart setStateOnPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();

        BlockState state = switch (face) {
            case DOWN -> defaultBlockState().setValue(LeverBlock.FACE, AttachFace.CEILING).setValue(LeverBlock.FACING, context.getHorizontalDirection());
            case UP -> defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR).setValue(LeverBlock.FACING, context.getHorizontalDirection());
            default -> defaultBlockState().setValue(LeverBlock.FACE, AttachFace.WALL).setValue(LeverBlock.FACING, face);
        };

        if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
            this.state = state;
            return this;
        }

        return null;
    }

    @Override
    public InteractionResult activate(Player player, PartRayTraceResult hit, ItemStack item, InteractionHand hand) {
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        state = state.cycle(LeverBlock.POWERED);
        level().playSound(null, pos(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, active() ? 0.6F : 0.5F);

        sendUpdate(this::writeDesc);
        tile().setChanged();
        tile().notifyPartChange(this);
        tile().notifyNeighborChange(getSide().ordinal());
        return InteractionResult.SUCCESS;
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
            LeverBlock.makeParticle(state, level(), pos(), 1.0F);
        }
    }
}
