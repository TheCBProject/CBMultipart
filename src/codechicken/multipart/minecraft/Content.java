package codechicken.multipart.minecraft;

import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.IPartConverter;
import codechicken.multipart.IPartFactory;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.Arrays;

public class Content implements IPartFactory, IPartConverter
{
    @Override
    public TMultiPart createPart(String name, boolean client)
    {
        if(name.equals("mc_torch")) return new TorchPart();
//        if(name.equals("mc_lever")) return new LeverPart();
//        if(name.equals("mc_button")) return new ButtonPart();
        if(name.equals("mc_redtorch")) return new RedstoneTorchPart();

        return null;
    }

    public void init()
    {
        MultiPartRegistry.registerConverter(this);
        MultiPartRegistry.registerParts(this, new String[]{
                "mc_torch",
                "mc_lever",
                "mc_button",
                "mc_redtorch"
            });
    }

    @Override
    public Iterable<Block> blockTypes() {
        return Arrays.asList(Blocks.TORCH, Blocks.LEVER, Blocks.STONE_BUTTON, Blocks.WOODEN_BUTTON, Blocks.REDSTONE_TORCH, Blocks.UNLIT_REDSTONE_TORCH);
    }

    @Override
    public TMultiPart convert(World world, BlockCoord pos)
    {
        IBlockState state = world.getBlockState(pos.pos());
        Block b = state.getBlock();

        if(b == Blocks.TORCH)
            return new TorchPart(state);
//        if(b == Blocks.lever)
//            return new LeverPart(meta);
//        if(b == Blocks.stone_button)
//            return new ButtonPart(meta);
//        if(b == Blocks.wooden_button)
//            return new ButtonPart(meta|0x10);
        if(b == Blocks.REDSTONE_TORCH)
            return new RedstoneTorchPart(state);
        if(b == Blocks.UNLIT_REDSTONE_TORCH)
            return new RedstoneTorchPart(state);

        return null;
    }
}
