package codechicken.multipart.wrapped.level;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/**
 * Created by covers1624 on 6/1/25.
 */
public interface WrapperLevel extends LevelAccessor {

    /**
     * @return The level we are wrapping.
     */
    Level wrapped();

    /**
     * @return The provider for what we are wrapping and where.
     */
    WrapperBlockProvider provider();

    /**
     * Pull all fields from the wrapped World into this world.
     */
    void pullContext();

    /**
     * Push all fields from this world into the wrapped world.
     */
    void pushContext();
}
