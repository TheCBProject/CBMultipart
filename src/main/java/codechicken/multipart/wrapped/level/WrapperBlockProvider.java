package codechicken.multipart.wrapped.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created by covers1624 on 6/4/25.
 */
// TODO this probably needs a better name. WrapperShim, or something.
public abstract class WrapperBlockProvider {

    public abstract BlockState getState(Level wrapped, BlockPos pos);

    public abstract boolean setState(Level wrapped, BlockPos pos, BlockState state, int flags, int recursionLeft);
}
