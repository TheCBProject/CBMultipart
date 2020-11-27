//package codechicken.multipart.minecraft;
//
//import codechicken.lib.util.ArrayUtils;
//import codechicken.multipart.MultiPartRegistry;
//import codechicken.multipart.api.factory.IPartConverter;
//import codechicken.multipart.api.factory.IPartFactory;
//import codechicken.multipart.api.factory.IPlacementConverter;
//import codechicken.multipart.api.part.TMultiPart;
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.Blocks;
//import net.minecraft.item.BlockItemUseContext;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Supplier;
//
//public class Content implements IPartFactory, IPartConverter, IPlacementConverter {
//
//    public static final ResourceLocation TORCH = new ResourceLocation("minecraft:torch");
//    public static final ResourceLocation LEVER = new ResourceLocation("minecraft:lever");
//    public static final ResourceLocation BUTTON = new ResourceLocation("minecraft:button");
//    public static final ResourceLocation REDTORCH = new ResourceLocation("minecraft:redtorch");
//    public static final Map<ResourceLocation, Supplier<TMultiPart>> parts = new HashMap<>();
//    //@formatter:off
//    private static final Block[] supported_blocks = {
//            Blocks.TORCH,
//            Blocks.LEVER,
//            Blocks.STONE_BUTTON,/* Blocks.WOODEN_BUTTON,*/
//            Blocks.REDSTONE_TORCH/*, Blocks.UNLIT_REDSTONE_TORCH*/
//    };
//    //@formatter:on
//
//    static {
//        parts.put(TORCH, TorchPart::new);
//        parts.put(LEVER, LeverPart::new);
//        parts.put(BUTTON, ButtonPart::new);
//        parts.put(REDTORCH, RedstoneTorchPart::new);
//    }
//
//    @Override
//    public TMultiPart createPart(ResourceLocation name, boolean client) {
//        if (parts.containsKey(name)) {
//            return parts.get(name).get();
//        }
//        return null;
//    }
//
//    public void init() {
//        MultiPartRegistry.registerConverter(this);
//        MultiPartRegistry.registerPlacementConverter(this);
//        MultiPartRegistry.registerParts(this, parts.keySet());
//    }
//
//    @Override
//    public boolean canConvert(World world, BlockPos pos, BlockState state) {
//        return ArrayUtils.contains(supported_blocks, state.getBlock());
//    }
//
//    @Override
//    public TMultiPart convert(World world, BlockPos pos, BlockState state) {
//        Block b = state.getBlock();
//
//        if (b == Blocks.TORCH) {
//            return new TorchPart(state);
//        }
//        if (b == Blocks.LEVER) {
//            return new LeverPart(state);
//        }
//        if (b == Blocks.STONE_BUTTON /*|| b == Blocks.WOODEN_BUTTON*/) {
//            return new ButtonPart(state);
//        }
//        if (b == Blocks.REDSTONE_TORCH/* || b == Blocks.UNLIT_REDSTONE_TORCH*/) {
//            return new RedstoneTorchPart(state);
//        }
//
//        return null;
//    }
//
//    @Override
//    public boolean canConvert(ItemStack stack) {
//        return ArrayUtils.contains(supported_blocks, Block.getBlockFromItem(stack.getItem()));
//    }
//
//    @Override
//    public TMultiPart convert(BlockItemUseContext context) {
//        McStatePart part = null;
//        Block heldBlock = Block.getBlockFromItem(context.getItem().getItem());
//        if (heldBlock == Blocks.TORCH) {
//            part = new TorchPart();
//        } else if (heldBlock == Blocks.LEVER) {
//            part = new LeverPart();
//        } else if (heldBlock == Blocks.STONE_BUTTON /*|| heldBlock == Blocks.WOODEN_BUTTON*/) {
//            part = new ButtonPart();
//        } else if (heldBlock == Blocks.REDSTONE_TORCH) {
//            part = new RedstoneTorchPart();
//        }
//        if (part != null) {
//            part.setStateOnPlacement(context);
//        }
//        return part;
//    }
//}
