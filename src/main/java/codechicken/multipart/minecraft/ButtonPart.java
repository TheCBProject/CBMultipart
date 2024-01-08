package codechicken.multipart.minecraft;

import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.redstone.FaceRedstonePart;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

public class ButtonPart extends McSidedStatePart implements FaceRedstonePart {

    private final MultipartType<?> type;
    private final ButtonBlock block;

    public ButtonPart(MultipartType<?> type, ButtonBlock block) {
        this(type, block, block.defaultBlockState());
    }

    public ButtonPart(MultipartType<?> type, ButtonBlock block, BlockState state) {
        super(state);
        this.type = type;
        this.block = block;
    }

    @Override
    public MultipartType<?> getType() {
        return type;
    }

    @Override
    public BlockState defaultBlockState() {
        return block.defaultBlockState();
    }

    @Override
    public ItemStack getDropStack() {
        return new ItemStack(block);
    }

    @Override
    public Direction getSide() {
        return FaceAttachedHorizontalDirectionalBlock.getConnectedDirection(state).getOpposite();
    }

    @Override
    public @Nullable MultiPart setStateOnPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();

        BlockState state = switch (face) {
            case DOWN -> defaultBlockState().setValue(ButtonBlock.FACE, AttachFace.CEILING).setValue(ButtonBlock.FACING, context.getHorizontalDirection());
            case UP -> defaultBlockState().setValue(ButtonBlock.FACE, AttachFace.FLOOR).setValue(ButtonBlock.FACING, context.getHorizontalDirection());
            default -> defaultBlockState().setValue(ButtonBlock.FACE, AttachFace.WALL).setValue(ButtonBlock.FACING, face);
        };

        if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
            this.state = state;
            return this;
        }

        return null;
    }

    public int delay() {
        return block.ticksToStayPressed;
    }

    public boolean canArrowsPress() {
        return block.arrowsCanPress;
    }

    @Override
    public InteractionResult activate(Player player, PartRayTraceResult hit, ItemStack item, InteractionHand hand) {
        if (pressed()) {
            return InteractionResult.CONSUME;
        }

        if (!level().isClientSide) {
            toggle();
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void scheduledTick() {
        if (pressed()) {
            updateState();
        }
    }

    public boolean pressed() {
        return state.getValue(ButtonBlock.POWERED);
    }

    @Override
    public void onEntityCollision(Entity entity) {
        if (!pressed() && !level().isClientSide && entity instanceof Arrow) {
            updateState();
        }
    }

    private void toggle() {
        state = state.cycle(ButtonBlock.POWERED);

        boolean on = pressed();

        block.playSound(null, level(), pos(), on);

        if (on) {
            scheduleTick(delay());
        }

        sendUpdate(this::writeDesc);
        tile().setChanged();
        tile().notifyPartChange(this);
        tile().notifyNeighborChange(getSide().ordinal());
    }

    private void updateState() {
        boolean arrows = canArrowsPress() && !level().getEntitiesOfClass(Arrow.class, getShape(CollisionContext.empty()).bounds().move(pos())).isEmpty();
        boolean pressed = pressed();

        if (arrows != pressed) {
            toggle();
        }
        if (arrows && pressed) {
            scheduleTick(delay());
        }
    }

    @Override
    public void onRemoved() {
        if (pressed()) {
            tile().notifyNeighborChange(getSide().ordinal());
        }
    }

    @Override
    public int weakPowerLevel(int side) {
        return pressed() ? 15 : 0;
    }

    @Override
    public int strongPowerLevel(int side) {
        return pressed() && side == getSide().ordinal() ? 15 : 0;
    }

    @Override
    public boolean canConnectRedstone(int side) {
        return true;
    }

    @Override
    public int getFace() {
        return getSide().ordinal();
    }

    public static class StoneButtonPart extends ButtonPart {

        public StoneButtonPart() { super(ModContent.stoneButtonPartType, (ButtonBlock) Blocks.STONE_BUTTON); }

        public StoneButtonPart(BlockState state) { super(ModContent.stoneButtonPartType, (ButtonBlock) Blocks.STONE_BUTTON, state); }
    }

    public static class PolishedBlackstoneButtonPart extends ButtonPart {

        public PolishedBlackstoneButtonPart() { super(ModContent.polishedBlackstoneButtonPartType, (ButtonBlock) Blocks.POLISHED_BLACKSTONE_BUTTON); }

        public PolishedBlackstoneButtonPart(BlockState state) { super(ModContent.polishedBlackstoneButtonPartType, (ButtonBlock) Blocks.POLISHED_BLACKSTONE_BUTTON, state); }
    }

    public static class OakButtonPart extends ButtonPart {

        public OakButtonPart() { super(ModContent.oakButtonPartType, (ButtonBlock) Blocks.OAK_BUTTON); }

        public OakButtonPart(BlockState state) { super(ModContent.oakButtonPartType, (ButtonBlock) Blocks.OAK_BUTTON, state); }
    }

    public static class SpruceButtonPart extends ButtonPart {

        public SpruceButtonPart() { super(ModContent.spruceButtonPartType, (ButtonBlock) Blocks.SPRUCE_BUTTON); }

        public SpruceButtonPart(BlockState state) { super(ModContent.spruceButtonPartType, (ButtonBlock) Blocks.SPRUCE_BUTTON, state); }
    }

    public static class BirchButtonPart extends ButtonPart {

        public BirchButtonPart() { super(ModContent.birchButtonPartType, (ButtonBlock) Blocks.BIRCH_BUTTON); }

        public BirchButtonPart(BlockState state) { super(ModContent.birchButtonPartType, (ButtonBlock) Blocks.BIRCH_BUTTON, state); }
    }

    public static class JungleButtonPart extends ButtonPart {

        public JungleButtonPart() { super(ModContent.jungleButtonPartType, (ButtonBlock) Blocks.JUNGLE_BUTTON); }

        public JungleButtonPart(BlockState state) { super(ModContent.jungleButtonPartType, (ButtonBlock) Blocks.JUNGLE_BUTTON, state); }
    }

    public static class AcaciaButtonPart extends ButtonPart {

        public AcaciaButtonPart() { super(ModContent.acaciaButtonPartType, (ButtonBlock) Blocks.ACACIA_BUTTON); }

        public AcaciaButtonPart(BlockState state) { super(ModContent.acaciaButtonPartType, (ButtonBlock) Blocks.ACACIA_BUTTON, state); }
    }

    public static class DarkOakButtonPart extends ButtonPart {

        public DarkOakButtonPart() { super(ModContent.darkOakButtonPartType, (ButtonBlock) Blocks.DARK_OAK_BUTTON); }

        public DarkOakButtonPart(BlockState state) { super(ModContent.darkOakButtonPartType, (ButtonBlock) Blocks.DARK_OAK_BUTTON, state); }
    }

    public static class CrimsonButtonPart extends ButtonPart {

        public CrimsonButtonPart() { super(ModContent.crimsonButtonPartType, (ButtonBlock) Blocks.CRIMSON_BUTTON); }

        public CrimsonButtonPart(BlockState state) { super(ModContent.crimsonButtonPartType, (ButtonBlock) Blocks.CRIMSON_BUTTON, state); }
    }

    public static class WarpedButtonPart extends ButtonPart {

        public WarpedButtonPart() { super(ModContent.warpedButtonPartType, (ButtonBlock) Blocks.WARPED_BUTTON); }

        public WarpedButtonPart(BlockState state) { super(ModContent.warpedButtonPartType, (ButtonBlock) Blocks.WARPED_BUTTON, state); }
    }

}
