package codechicken.microblock.part.placement;

import codechicken.microblock.part.MicroblockPart;
import codechicken.multipart.block.TileMultiPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Created by covers1624 on 20/10/22.
 */
public abstract class ExecutablePlacement {

    public final BlockPos pos;
    public final MicroblockPart part;

    protected ExecutablePlacement(BlockPos pos, MicroblockPart part) {
        this.pos = pos;
        this.part = part;
    }

    public abstract void place(Level level, Player player, ItemStack stack);

    public abstract void consume(Level level, Player player, ItemStack stack);

    public static class AdditionPlacement extends ExecutablePlacement {

        public AdditionPlacement(BlockPos pos, MicroblockPart part) {
            super(pos, part);
        }

        @Override
        public void place(Level level, Player player, ItemStack stack) {
            TileMultiPart.addPart(level, pos, part);
        }

        @Override
        public void consume(Level level, Player player, ItemStack stack) {
            stack.shrink(1);
        }
    }

    public static class ExpandingPlacement extends ExecutablePlacement {

        private final MicroblockPart oPart;

        public ExpandingPlacement(BlockPos pos, MicroblockPart nPart, MicroblockPart oPart) {
            super(pos, nPart);
            this.oPart = oPart;
        }

        @Override
        public void place(Level level, Player player, ItemStack stack) {
            oPart.shape = part.shape;
            oPart.tile().notifyPartChange(oPart);
            oPart.sendShapeUpdate();
        }

        @Override
        public void consume(Level level, Player player, ItemStack stack) {
            stack.shrink(1);
        }
    }
}
