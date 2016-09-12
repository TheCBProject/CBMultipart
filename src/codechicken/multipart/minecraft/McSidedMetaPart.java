package codechicken.multipart.minecraft;

import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.TFacePart;
import codechicken.multipart.TileMultipart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public abstract class McSidedMetaPart extends McMetaPart implements TFacePart
{
    public McSidedMetaPart()
    {
    }

    public McSidedMetaPart(IBlockState state)
    {
        super(state);
    }

    public abstract int getSideFromState();

    @Override
    public void onNeighborChanged()
    {
        if(!world().isRemote)
            dropIfCantStay();
    }

    public boolean canStay()
    {
        BlockCoord pos = new BlockCoord(tile()).offset(getSideFromState());
        return world().isSideSolid(pos.pos(), EnumFacing.VALUES[getSideFromState()]);
    }

    public boolean dropIfCantStay()
    {
        if(!canStay()) {
            drop();
            return true;
        }
        return false;
    }

    public void drop()
    {
        TileMultipart.dropItem(new ItemStack(getBlock()), world(), Vector3.fromTileCenter(tile()));
        tile().remPart(this);
    }

    @Override
    public int getSlotMask()
    {
        return 1<<getSideFromState();
    }

    @Override
    public boolean solid(int side)
    {
        return false;
    }

    @Override
    public int redstoneConductionMap()
    {
        return 0x1F;
    }
}
