package codechicken.multipart.api.part;

import codechicken.multipart.block.TileMultiPart;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 6/6/22.
 */
public abstract class AbstractMultiPart implements TMultiPart {

    @Nullable
    private TileMultiPart tile;

    @Override
    public final TileMultiPart tile() {
        assert tile != null;
        return tile;
    }

    @Override
    public final boolean hasTile() {
        return tile != null;
    }

    public final void bind(TileMultiPart tile) {
        this.tile = tile;
    }
}
