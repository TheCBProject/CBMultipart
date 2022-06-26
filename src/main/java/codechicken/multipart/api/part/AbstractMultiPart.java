package codechicken.multipart.api.part;

import codechicken.multipart.block.TileMultiPart;

/**
 * Created by covers1624 on 6/6/22.
 */
public abstract class AbstractMultiPart implements TMultiPart {

    private TileMultiPart tile;

    @Override
    public final TileMultiPart tile() {
        return tile;
    }

    public final void bind(TileMultiPart tile) {
        this.tile = tile;
    }
}
