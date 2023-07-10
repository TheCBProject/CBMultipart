package codechicken.multipart.trait;

import codechicken.mixin.forge.TraitSide;
import codechicken.multipart.api.annotation.MultiPartTrait;
import codechicken.multipart.api.part.AnimateTickPart;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import net.minecraft.util.RandomSource;

import java.util.List;

/**
 * Created by covers1624 on 2/9/20.
 */
@MultiPartTrait (value = AnimateTickPart.class, side = TraitSide.CLIENT)
public class TAnimateTickTile extends TileMultipart {

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
