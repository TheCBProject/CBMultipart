package codechicken.multipart.api.part;

import codechicken.multipart.block.TileMultipart;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 6/6/22.
 */
public abstract class BaseMultipart implements MultiPart {

    @Nullable
    private TileMultipart tile;

    @Override
    public final TileMultipart tile() {
        assert tile != null;
        return tile;
    }

    @Override
    public final boolean hasTile() {
        return tile != null;
    }

    public final void bind(TileMultipart tile) {
        this.tile = tile;
    }
}
