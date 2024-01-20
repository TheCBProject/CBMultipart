package codechicken.multipart.api.part;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;

/**
 * Parts that need to do random animation ticks can implement this.
 * This is passed from {@link Block#animateTick}.
 * <p>
 * Created by covers1624 on 2/9/20.
 */
public interface AnimateTickPart extends MultiPart {

    void animateTick(RandomSource random);
}
