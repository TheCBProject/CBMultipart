package codechicken.multipart.minecraft;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.multipart.IFaceRedstonePart;
import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ButtonPart extends McSidedMetaPart implements IFaceRedstonePart
{
    public static BlockButton stoneButton = (BlockButton) Blocks.STONE_BUTTON;
    public static BlockButton woodenButton = (BlockButton) Blocks.WOODEN_BUTTON;

    public ButtonPart()
    {
        state = stoneButton.getDefaultState();
    }

    public ButtonPart(IBlockState state)
    {
        super(state);
    }

    @Override
    public String getType()
    {
        return "mc_button";
    }

    @Override
    public byte getMeta()
    {
        int m = getBlock().getMetaFromState(state);
        if (sensitive()) m |= 1<<7;
        return (byte) m;
    }

    @Override
    public void setMeta(byte meta)
    {
        state = ((meta&1<<7) != 0 ? woodenButton : stoneButton).getStateFromMeta(meta&0x7F);
    }

    @Override
    public Block getBlock()
    {
        return sensitive() ? woodenButton : stoneButton;
    }

    @Override
    public int getSideFromState()
    {
        return state.getValue(BlockButton.FACING).getOpposite().ordinal();
    }

    public int delay()
    {
        return sensitive() ? 30 : 20;
    }

    public boolean sensitive()
    {
        return state.getBlock() == woodenButton;
    }

    @Override
    public void setStateOnPlacement(World world, BlockPos pos, EnumFacing facing, Vec3d hitVec, EntityLivingBase placer, ItemStack held)
    {
        Block heldBlock = Block.getBlockFromItem(held.getItem());
        if (!(heldBlock instanceof  BlockButton))
            throw new RuntimeException("Invalid placement of Button Part");
        state = heldBlock.onBlockPlaced(world, pos, facing, (float)hitVec.xCoord, (float)hitVec.yCoord, (float)hitVec.zCoord, 0, placer);
    }

    @Override
    public boolean activate(EntityPlayer player, CuboidRayTraceResult hit, ItemStack item, EnumHand hand)
    {
        if(pressed())
            return false;

        if(!world().isRemote)
            toggle();

        return true;
    }

    @Override
    public void scheduledTick()
    {
        if(pressed())
            updateState();
    }

    public boolean pressed()
    {
        return state.getValue(BlockButton.POWERED);
    }

    @Override
    public void onEntityCollision(Entity entity)
    {
        if(!pressed() && !world().isRemote && entity instanceof EntityArrow)
            updateState();
    }

    private void toggle()
    {
        state = state.cycleProperty(BlockButton.POWERED);

        boolean on = pressed();

        SoundEvent sound = sensitive() ? (on ? SoundEvents.BLOCK_WOOD_BUTTON_CLICK_ON : SoundEvents.BLOCK_WOOD_BUTTON_CLICK_OFF) :
                (on ? SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON : SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF);

        world().playSound(null, pos(), sound, SoundCategory.BLOCKS, 0.3F, on ? 0.6F : 0.5F);

        if(on)
            scheduleTick(delay());

        sendDescUpdate();
        tile().markDirty();
        tile().notifyPartChange(this);
        tile().notifyNeighborChange(getSideFromState());
    }

    private void updateState()
    {
        boolean arrows = sensitive() && !world().getEntitiesWithinAABB(EntityArrow.class,
                getBounds().add(pos()).aabb()).isEmpty();
        boolean pressed = pressed();

        if(arrows != pressed)
            toggle();
        if(arrows && pressed)
            scheduleTick(delay());
    }

    @Override
    public void onRemoved()
    {
        if(pressed())
            tile().notifyNeighborChange(getSideFromState());
    }

    @Override
    public int weakPowerLevel(int side)
    {
        return pressed() ? 15 : 0;
    }

    @Override
    public int strongPowerLevel(int side)
    {
        return pressed() && side == getSideFromState() ? 15 : 0;
    }

    @Override
    public boolean canConnectRedstone(int side)
    {
        return true;
    }

    @Override
    public int getFace() {
        return getSideFromState();
    }
}
