package codechicken.multipart.api;

import codechicken.multipart.block.BlockMultiPart;

/**
 * Internal interface for identifying tiles owned by {@link BlockMultiPart}, which can tick.
 * <p>
 * Created by covers1624 on 23/10/22.
 */
public interface ITickableTile {

    void tick();
}
