package codechicken.multipart.api.part;

import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 7/1/21.
 */
public interface CapabilityProviderPart extends MultiPart {

    <T, C> @Nullable T getCapability(BlockCapability<T, C> capability, C context);
}
