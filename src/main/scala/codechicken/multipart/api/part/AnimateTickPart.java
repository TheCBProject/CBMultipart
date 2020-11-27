package codechicken.multipart.api.part;

import net.minecraft.block.Block;

import java.util.Random;

/**
 * Parts that need to do random animation ticks can implement this.
 * This is passed from {@link Block#animateTick}.
 * <p>
 * Created by covers1624 on 2/9/20.
 */
public interface AnimateTickPart {

    void animateTick(Random random);
}
