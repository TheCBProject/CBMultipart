package codechicken.multipart.util;

import net.minecraft.item.ItemUseContext;
import net.minecraft.util.math.BlockPos;

/**
 * Created by covers1624 on 1/1/21.
 */
public class OffsetItemUseContext extends ItemUseContext {

    private final BlockPos pos;

    public OffsetItemUseContext(ItemUseContext other) {
        super(other.getLevel(), other.getPlayer(), other.getHand(), other.getItemInHand(), other.hitResult);
        this.pos = other.getClickedPos().relative(getClickedFace());
    }

    @Override
    public BlockPos getClickedPos() {
        return pos;
    }
}
