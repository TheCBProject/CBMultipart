package codechicken.multipart.minecraft;

import codechicken.multipart.IRandomDisplayTickPart;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class TorchPart extends McSidedMetaPart implements IRandomDisplayTickPart {

	public static BlockTorch torch = (BlockTorch) Blocks.TORCH;

	public TorchPart() {
		state = torch.getDefaultState();
	}

	public TorchPart(IBlockState state) {
		super(state);
	}

	@Override
	public Block getBlock() {
		return torch;
	}

	@Override
	public ResourceLocation getType() {
		return new ResourceLocation("minecraft:torch");
	}

	@Override
	public int getSideFromState() {
		return state.getValue(BlockTorch.FACING).getOpposite().ordinal();
	}

	@Override
	public boolean canStay() {
		if (getSideFromState() == 0) {
			BlockPos offset = pos().offset(EnumFacing.DOWN);
			IBlockState state = world().getBlockState(offset);
			if (state.getBlock().canPlaceTorchOnTop(state, world(), offset)) {
				return true;
			}
		}
		return super.canStay();
	}

	@Override
	public void randomDisplayTick(Random random) {
		getBlock().randomDisplayTick(state, world(), pos(), random);
	}
}
