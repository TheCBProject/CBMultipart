package codechicken.multipart.trait;

import codechicken.multipart.api.part.AnimateTickPart;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Created by covers1624 on 2/9/20.
 */
public class TAnimateTickTile extends TileMultipart {

    public TAnimateTickTile(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void animateTick(RandomSource random) {
        List<MultiPart> jPartList = getPartList();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < jPartList.size(); i++) {
            MultiPart part = jPartList.get(i);
            if (part instanceof AnimateTickPart) {
                ((AnimateTickPart) part).animateTick(random);
            }
        }
    }
}
