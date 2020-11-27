package codechicken.multipart.trait;

import codechicken.mixin.forge.TraitSide;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.TileMultipartClient;
import codechicken.multipart.api.annotation.MultiPartTrait;
import codechicken.multipart.api.part.AnimateTickPart;
import codechicken.multipart.api.part.TMultiPart;

import java.util.List;
import java.util.Random;

/**
 * Created by covers1624 on 2/9/20.
 */
@MultiPartTrait (value = AnimateTickPart.class, side = TraitSide.CLIENT)
class TAnimateTickTile extends TileMultipart implements TileMultipartClient {

    @Override
    public void animateTick(Random random) {
        List<TMultiPart> jPartList = jPartList();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < jPartList.size(); i++) {
            TMultiPart part = jPartList.get(i);
            if (part instanceof AnimateTickPart) {
                ((AnimateTickPart) part).animateTick(random);
            }
        }
    }
}
