package codechicken.multipart.minecraft;

import codechicken.multipart.api.MultipartType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SoulTorchPart extends TorchPart {

    public SoulTorchPart() {
    }

    public SoulTorchPart(BlockState state) {
        super(state);
    }

    @Override
    protected Block getStandingBlock() {
        return Blocks.SOUL_TORCH;
    }

    @Override
    protected Block getWallBlock() {
        return Blocks.SOUL_WALL_TORCH;
    }

    @Override
    public MultipartType<?> getType() {
        return MinecraftMultipartModContent.SOUL_TORCH_PART.get();
    }
}
