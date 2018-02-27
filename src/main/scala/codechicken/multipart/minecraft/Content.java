package codechicken.multipart.minecraft;

import codechicken.lib.util.ArrayUtils;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.api.IPartConverter;
import codechicken.multipart.api.IPartFactory;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Content implements IPartFactory, IPartConverter {

    private static final ResourceLocation TORCH = new ResourceLocation("minecraft:torch");
    private static final ResourceLocation LEVER = new ResourceLocation("minecraft:lever");
    private static final ResourceLocation BUTTON = new ResourceLocation("minecraft:button");
    private static final ResourceLocation REDTORCH = new ResourceLocation("minecraft:redtorch");
    private static final Map<ResourceLocation, Supplier<TMultiPart>> parts = new HashMap<>();
    //@formatter:off
    private static final Block[] supported_blocks = {
            Blocks.TORCH,
            Blocks.LEVER,
            Blocks.STONE_BUTTON, Blocks.WOODEN_BUTTON,
            Blocks.REDSTONE_TORCH, Blocks.UNLIT_REDSTONE_TORCH
    };
    //@formatter:on

    static {
        parts.put(TORCH, TorchPart::new);
        parts.put(LEVER, LeverPart::new);
        parts.put(BUTTON, ButtonPart::new);
        parts.put(REDTORCH, RedstoneTorchPart::new);
    }

    @Override
    public TMultiPart createPart(ResourceLocation name, boolean client) {
        if (parts.containsKey(name)) {
            return parts.get(name).get();
        }
        return null;
    }

    public void init() {
        MultiPartRegistry.registerConverter(this);
        MultiPartRegistry.registerParts(this, parts.keySet());
    }

    @Override
    public boolean canConvert(World world, BlockPos pos, IBlockState state) {
        return ArrayUtils.contains(supported_blocks, state.getBlock());
    }

    @Override
    public TMultiPart convert(World world, BlockPos pos, IBlockState state) {
        Block b = state.getBlock();

        if (b == Blocks.TORCH) {
            return new TorchPart(state);
        }
        if (b == Blocks.LEVER) {
            return new LeverPart(state);
        }
        if (b == Blocks.STONE_BUTTON || b == Blocks.WOODEN_BUTTON) {
            return new ButtonPart(state);
        }
        if (b == Blocks.REDSTONE_TORCH || b == Blocks.UNLIT_REDSTONE_TORCH) {
            return new RedstoneTorchPart(state);
        }

        return null;
    }
}
