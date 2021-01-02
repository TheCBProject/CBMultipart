package codechicken.multipart.util;

import net.minecraft.item.ItemUseContext;
import net.minecraft.util.math.BlockPos;

/**
 * Created by covers1624 on 1/1/21.
 */
public class OffsetItemUseContext extends ItemUseContext {

    private final BlockPos pos;

    public OffsetItemUseContext(ItemUseContext other) {
        super(other.getWorld(), other.getPlayer(), other.getHand(), other.getItem(), other.rayTraceResult);
        this.pos = other.getPos().offset(getFace());
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }
}
