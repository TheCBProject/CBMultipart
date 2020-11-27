//package codechicken.multipart.minecraft;
//
//import codechicken.lib.raytracer.CuboidRayTraceResult;
//import codechicken.multipart.IFaceRedstonePart;
//import net.minecraft.block.*;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.entity.projectile.ArrowEntity;
//import net.minecraft.item.BlockItemUseContext;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.*;
//
//public class ButtonPart extends McSidedMetaPart implements IFaceRedstonePart {
//
//    public static StoneButtonBlock stoneButton = (StoneButtonBlock) Blocks.STONE_BUTTON;
//    public static WoodButtonBlock woodenButton = null;//(WoodButtonBlock) Blocks.WOODEN_BUTTON;
//
//    public ButtonPart() {
//        state = stoneButton.getDefaultState();
//    }
//
//    public ButtonPart(BlockState state) {
//        super(state);
//    }
//
//    @Override
//    public ResourceLocation getType() {
//        return Content.BUTTON;
//    }
//
//    @Override
//    public Block getBlock() {
//        return sensitive() ? woodenButton : stoneButton;
//    }
//
//    @Override
//    public Direction getSideFromState() {
//        return state.get(AbstractButtonBlock.HORIZONTAL_FACING).getOpposite();
//    }
//
//    public int delay() {
//        return sensitive() ? 30 : 20;
//    }
//
//    public boolean sensitive() {
//        return state.getBlock() == woodenButton;
//    }
//
//    @Override
//    public void setStateOnPlacement(BlockItemUseContext context) {
//        Block heldBlock = Block.getBlockFromItem(context.getItem().getItem());
//        if (!(heldBlock instanceof AbstractButtonBlock)) {
//            throw new RuntimeException("Invalid placement of Button Part");
//        }
//        state = heldBlock.getStateForPlacement(context);
//    }
//
//    @Override
//    public boolean activate(PlayerEntity player, CuboidRayTraceResult hit, ItemStack item, Hand hand) {
//        if (pressed()) {
//            return false;
//        }
//
//        if (!world().isRemote) {
//            toggle();
//        }
//
//        return true;
//    }
//
//    @Override
//    public void scheduledTick() {
//        if (pressed()) {
//            updateState();
//        }
//    }
//
//    public boolean pressed() {
//        return state.get(AbstractButtonBlock.POWERED);
//    }
//
//    @Override
//    public void onEntityCollision(Entity entity) {
//        if (!pressed() && !world().isRemote && entity instanceof ArrowEntity) {
//            updateState();
//        }
//    }
//
//    private void toggle() {
//        state = state.cycle(AbstractButtonBlock.POWERED);
//
//        boolean on = pressed();
//
//        SoundEvent sound = sensitive() ? (on ? SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON : SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_OFF) : (on ? SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON : SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF);
//
//        world().playSound(null, pos(), sound, SoundCategory.BLOCKS, 0.3F, on ? 0.6F : 0.5F);
//
//        if (on) {
//            scheduleTick(delay());
//        }
//
//        sendDescUpdate();
//        tile().markDirty();
//        tile().notifyPartChange(this);
//        tile().notifyNeighborChange(getSideFromState().ordinal());
//    }
//
//    private void updateState() {
//        boolean arrows = sensitive() && !world().getEntitiesWithinAABB(ArrowEntity.class, getBounds().add(pos()).aabb()).isEmpty();
//        boolean pressed = pressed();
//
//        if (arrows != pressed) {
//            toggle();
//        }
//        if (arrows && pressed) {
//            scheduleTick(delay());
//        }
//    }
//
//    @Override
//    public void onRemoved() {
//        if (pressed()) {
//            tile().notifyNeighborChange(getSideFromState().ordinal());
//        }
//    }
//
//    @Override
//    public int weakPowerLevel(int side) {
//        return pressed() ? 15 : 0;
//    }
//
//    @Override
//    public int strongPowerLevel(int side) {
//        return pressed() && side == getSideFromState().ordinal() ? 15 : 0;
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
