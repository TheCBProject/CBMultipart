package codechicken.multipart.minecraft;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.IFaceRedstonePart;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLever;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;

public class LeverPart extends McSidedMetaPart implements IFaceRedstonePart
{
    public static BlockLever lever = (BlockLever) Blocks.LEVER;

    public static Cuboid6[][] bounds = new Cuboid6[6][2];

    static {
        //Because vanilla bounds are wierd and dont allow nice multipart fitting
        bounds[0][0] = new Cuboid6(5/16D, 0/16D, 3/16D, 11/16D, 6/16D, 13/16D);
        for (int r = 0; r < 2; r++)
            for (int s = 0; s < 6; s++)
                bounds[s][r] = bounds[0][0].copy().apply(
                        Rotation.sideOrientation(s, r).at(Vector3.center));
    }

    public LeverPart()
    {
        state = lever.getDefaultState();
    }

    public LeverPart(IBlockState state)
    {
        super(state);
    }

    @Override
    public Block getBlock()
    {
        return lever;
    }

    @Override
    public String getType()
    {
        return "mc_lever";
    }

    public boolean active()
    {
        return state.getValue(BlockLever.POWERED);
    }

    @Override
    public int getSideFromState()
    {
        return state.getValue(BlockLever.FACING).getFacing().getOpposite().ordinal();
    }

    @Override
    public boolean activate(EntityPlayer player, CuboidRayTraceResult hit, ItemStack item, EnumHand hand)
    {
        if(world().isRemote)
            return true;

        state = state.cycleProperty(BlockLever.POWERED);
        world().playSound(null, pos(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, active() ? 0.6F : 0.5F);

        sendDescUpdate();
        tile().markDirty();
        tile().notifyPartChange(this);
        tile().notifyNeighborChange(getSideFromState());
        return true;
    }

    @Override
    public void onRemoved()
    {
        if(active())
            tile().notifyNeighborChange(getSideFromState());
    }

    @Override
    public void onConverted()
    {
        if(active())
            tile().notifyNeighborChange(getSideFromState());
    }

    @Override
    public Cuboid6 getBounds()
    {
        BlockLever.EnumOrientation facing = state.getValue(BlockLever.FACING);
        int r = facing == BlockLever.EnumOrientation.DOWN_X ||
                    facing == BlockLever.EnumOrientation.UP_X ? 1 : 0;

        return bounds[getSideFromState()][r];
    }

    @Override
    public int weakPowerLevel(int side)
    {
        return active() ? 15 : 0;
    }

    @Override
    public int strongPowerLevel(int side)
    {
        return active() && side == getSideFromState() ? 15 : 0;
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
