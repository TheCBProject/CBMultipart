package codechicken.multipart.minecraft;

import codechicken.lib.vec.Vector3;
import codechicken.multipart.api.part.FacePart;
import codechicken.multipart.block.TileMultipart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public abstract class McSidedStatePart extends McStatePart implements FacePart {

    public McSidedStatePart() {
    }

    public McSidedStatePart(BlockState state) {
        super(state);
    }

    public abstract Direction getSide();

    @Override
    public void onNeighborBlockChanged(BlockPos from) {
        if (!level().isClientSide) {
            dropIfCantStay();
        }
    }

    public boolean canStay() {
        return state.canSurvive(level(), pos());
    }

    public boolean dropIfCantStay() {
        if (!canStay()) {
            drop();
            return true;
        }
        return false;
    }

    public void drop() {
        TileMultipart.dropItem(getDropStack(), level(), Vector3.fromTileCenter(tile()));
        tile().remPart(this);
    }

    @Override
    public int getSlotMask() {
        return 1 << getSide().ordinal();
    }

    @Override
    public int redstoneConductionMap() {
        return 0x1F;
    }

    protected BlockState transformFaceAttachedHorizontalDirectionBlock(BlockState state, Direction.Axis rotationAxis, Rotation rotation, Mirror mirror) {
        if (mirror != Mirror.NONE) {
            state = state.mirror(mirror);
        }

        var stateFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        var stateFace = state.getValue(BlockStateProperties.ATTACH_FACE);
        var z = rotationAxis == Direction.Axis.Z;
        var forcedAxis = z ? Direction.WEST : Direction.SOUTH;

        if (stateFacing.getAxis() == rotationAxis && stateFace == AttachFace.WALL)
            return state;

        for (int i = 0; i < rotation.ordinal(); i++) {
            stateFace = state.getValue(BlockStateProperties.ATTACH_FACE);
            stateFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

            boolean b = state.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.CEILING;
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, b ? forcedAxis : forcedAxis.getOpposite());

            if (stateFace != AttachFace.WALL) {
                state = state.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL);
                continue;
            }

            if (stateFacing.getAxisDirection() == (z ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE)) {
                state = state.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR);
                continue;
            }
            state = state.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.CEILING);
        }
        return state;
    }
}
