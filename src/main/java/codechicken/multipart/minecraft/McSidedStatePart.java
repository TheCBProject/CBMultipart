package codechicken.multipart.minecraft;

import codechicken.lib.vec.Vector3;
import codechicken.multipart.api.part.TFacePart;
import codechicken.multipart.block.TileMultiPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public abstract class McSidedStatePart extends McStatePart implements TFacePart {

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
        TileMultiPart.dropItem(getDropStack(), level(), Vector3.fromTileCenter(tile()));
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
}
