package codechicken.multipart.minecraft;

import codechicken.lib.raytracer.VoxelShapeCache;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.api.MultiPartType;
import codechicken.multipart.api.part.AnimateTickPart;
import codechicken.multipart.api.part.TMultiPart;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorldReader;

import java.util.Collections;
import java.util.Random;

public class TorchPart extends McSidedStatePart implements AnimateTickPart {

    public static VoxelShape STANDING_OCCLUSION;
    public static VoxelShape[] WALL_OCCLUSION = new VoxelShape[4];

    //Old style 1.12 and prior boxes for occlusion.
    // They more accurately represent the torches size.
    static {
        STANDING_OCCLUSION = VoxelShapeCache.getShape(new Cuboid6(0.4D, 0.0D, 0.4D, 0.6D, 0.6D, 0.6D));
        Cuboid6 wall = new Cuboid6(0.35D, 0.2D, 0.7D, 0.65D, 0.8D, 1.0D);
        for (int i = 0; i < 4; i++) {
            WALL_OCCLUSION[i] = VoxelShapeCache.getShape(wall.copy().apply(Rotation.quarterRotations[i].at(Vector3.CENTER)));
        }
    }

    public TorchPart() {
    }

    public TorchPart(BlockState state) {
        super(state);
    }

    @Override
    public MultiPartType<?> getType() {
        return ModContent.torchPartType;
    }

    protected Block getStandingBlock() {
        return Blocks.TORCH;
    }

    protected Block getWallBlock() {
        return Blocks.WALL_TORCH;
    }

    @Override
    public BlockState defaultBlockState() {
        return getStandingBlock().defaultBlockState();
    }

    @Override
    public ItemStack getDropStack() {
        return new ItemStack(getStandingBlock());
    }

    @Override
    public Direction getSide() {
        return state.getBlock() == getStandingBlock() ? Direction.DOWN : state.getValue(HorizontalBlock.FACING).getOpposite();
    }

    @Override
    public VoxelShape getOcclusionShape() {
        if (state.getBlock() == getStandingBlock()) {
            return STANDING_OCCLUSION;
        }
        return WALL_OCCLUSION[getSide().get2DDataValue()];
    }

    @Override
    public TMultiPart setStateOnPlacement(BlockItemUseContext context) {
        BlockState wallState = getWallBlock().getStateForPlacement(context);

        IWorldReader world = context.getLevel();
        BlockPos pos = context.getClickedPos();

        for (Direction dir : context.getNearestLookingDirections()) {
            if (dir != Direction.UP) {
                BlockState state = dir == Direction.DOWN ? getStandingBlock().getStateForPlacement(context) : wallState;
                if (state != null && state.canSurvive(world, pos)) {
                    this.state = state;
                    return this;
                }
            }
        }

        return null;
    }

    @Override
    public void animateTick(Random random) {
        state.getBlock().animateTick(state, world(), pos(), random);
    }
}
