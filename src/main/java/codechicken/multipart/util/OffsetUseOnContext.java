package codechicken.multipart.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Created by covers1624 on 1/1/21.
 */
@Deprecated(since = "1.18.2", forRemoval = true) // Use MultipartPlaceContext
public class OffsetUseOnContext extends UseOnContext {

    private final BlockPos pos;

    public OffsetUseOnContext(UseOnContext other) {
        super(other.getLevel(), other.getPlayer(), other.getHand(), other.getItemInHand(), other.getHitResult());
        this.pos = other.getClickedPos().relative(getClickedFace());
    }

    @Override
    public BlockPos getClickedPos() {
        return pos;
    }
}
