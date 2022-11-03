package codechicken.multipart.api;

import codechicken.multipart.block.BlockMultipart;

/**
 * Internal interface for identifying tiles owned by {@link BlockMultipart}, which can tick.
 * <p>
 * Created by covers1624 on 23/10/22.
 */
public interface TickableTile {

    void tick();
}
