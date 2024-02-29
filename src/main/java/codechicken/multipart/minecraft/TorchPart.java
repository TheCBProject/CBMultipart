package codechicken.multipart.minecraft;

import codechicken.lib.raytracer.VoxelShapeCache;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.part.AnimateTickPart;
import codechicken.multipart.api.part.MultiPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

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
    public MultipartType<?> getType() {
        return ModContent.TORCH_PART.get();
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
        return state.getBlock() == getStandingBlock() ? Direction.DOWN : state.getValue(HorizontalDirectionalBlock.FACING).getOpposite();
    }

    @Override
    public VoxelShape getOcclusionShape() {
        if (state.getBlock() == getStandingBlock()) {
            return STANDING_OCCLUSION;
        }
        return WALL_OCCLUSION[getSide().get2DDataValue()];
    }

    @Nullable
    @Override
    public MultiPart setStateOnPlacement(BlockPlaceContext context) {

        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();

        if (face == Direction.DOWN) return null;

        BlockState state = face == Direction.UP ?
                getStandingBlock().defaultBlockState() :
                getWallBlock().defaultBlockState().setValue(WallTorchBlock.FACING, face);

        if (state.canSurvive(world, pos)) {
            this.state = state;
            return this;
        }

        return null;
    }

    @Override
    public void animateTick(RandomSource random) {
        state.getBlock().animateTick(state, level(), pos(), random);
    }
}
