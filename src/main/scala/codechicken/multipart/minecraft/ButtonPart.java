package codechicken.multipart.minecraft;

import codechicken.multipart.api.MultiPartType;
import codechicken.multipart.api.part.redstone.IFaceRedstonePart;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;


public class ButtonPart extends McSidedStatePart implements IFaceRedstonePart {

    private final MultiPartType<?> type;
    private final AbstractButtonBlock block;

    public ButtonPart(MultiPartType<?> type, AbstractButtonBlock block) {
        this(type, block, block.defaultBlockState());
    }

    public ButtonPart(MultiPartType<?> type, AbstractButtonBlock block, BlockState state) {
        super(state);
        this.type = type;
        this.block = block;
    }

    @Override
    public MultiPartType<?> getType() {
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
        return HorizontalFaceBlock.getConnectedDirection(state).getOpposite();
    }

    public int delay() {
        return sensitive() ? 30 : 20;
    }

    public boolean sensitive() {
        return block.sensitive;
    }

    @Override
    public ActionResultType activate(PlayerEntity player, PartRayTraceResult hit, ItemStack item, Hand hand) {
        if (pressed()) {
            return ActionResultType.CONSUME;
        }

        if (!world().isClientSide) {
            toggle();
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public void scheduledTick() {
        if (pressed()) {
            updateState();
        }
    }

    public boolean pressed() {
        return state.getValue(AbstractButtonBlock.POWERED);
    }

    @Override
    public void onEntityCollision(Entity entity) {
        if (!pressed() && !world().isClientSide && entity instanceof ArrowEntity) {
            updateState();
        }
    }

    private void toggle() {
        state = state.cycle(AbstractButtonBlock.POWERED);

        boolean on = pressed();

        block.playSound(null, world(), pos(), on);

        if (on) {
            scheduleTick(delay());
        }

        sendUpdate(this::writeDesc);
        tile().setChanged();
        tile().notifyPartChange(this);
        tile().notifyNeighborChange(getSide().ordinal());
    }

    private void updateState() {
        boolean arrows = sensitive() && !world().getEntitiesOfClass(ArrowEntity.class, getOutlineShape().bounds().move(pos())).isEmpty();
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
        public StoneButtonPart() { super(ModContent.stoneButtonPartType, (AbstractButtonBlock) Blocks.STONE_BUTTON); }
        public StoneButtonPart(BlockState state) { super(ModContent.stoneButtonPartType, (AbstractButtonBlock) Blocks.STONE_BUTTON, state); }
    }

    public static class OakButtonPart extends ButtonPart {
        public OakButtonPart() { super(ModContent.oakButtonPartType, (AbstractButtonBlock) Blocks.OAK_BUTTON); }
        public OakButtonPart(BlockState state) { super(ModContent.oakButtonPartType, (AbstractButtonBlock) Blocks.OAK_BUTTON, state); }
    }

    public static class SpruceButtonPart extends ButtonPart {
        public SpruceButtonPart() { super(ModContent.spruceButtonPartType, (AbstractButtonBlock) Blocks.SPRUCE_BUTTON); }
        public SpruceButtonPart(BlockState state) { super(ModContent.spruceButtonPartType, (AbstractButtonBlock) Blocks.SPRUCE_BUTTON, state); }
    }

    public static class BirchButtonPart extends ButtonPart {
        public BirchButtonPart() { super(ModContent.birchButtonPartType, (AbstractButtonBlock) Blocks.BIRCH_BUTTON); }
        public BirchButtonPart(BlockState state) { super(ModContent.birchButtonPartType, (AbstractButtonBlock) Blocks.BIRCH_BUTTON, state); }
    }

    public static class JungleButtonPart extends ButtonPart {
        public JungleButtonPart() { super(ModContent.jungleButtonPartType, (AbstractButtonBlock) Blocks.JUNGLE_BUTTON); }
        public JungleButtonPart(BlockState state) { super(ModContent.jungleButtonPartType, (AbstractButtonBlock) Blocks.JUNGLE_BUTTON, state); }
    }

    public static class AcaciaButtonPart extends ButtonPart {
        public AcaciaButtonPart() { super(ModContent.acaciaButtonPartType, (AbstractButtonBlock) Blocks.ACACIA_BUTTON); }
        public AcaciaButtonPart(BlockState state) { super(ModContent.acaciaButtonPartType, (AbstractButtonBlock) Blocks.ACACIA_BUTTON, state); }
    }

    public static class DarkOakButtonPart extends ButtonPart {
        public DarkOakButtonPart() { super(ModContent.darkOakButtonPartType, (AbstractButtonBlock) Blocks.DARK_OAK_BUTTON); }
        public DarkOakButtonPart(BlockState state) { super(ModContent.darkOakButtonPartType, (AbstractButtonBlock) Blocks.DARK_OAK_BUTTON, state); }
    }

}
