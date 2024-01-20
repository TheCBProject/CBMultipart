package codechicken.multipart.api.part;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 7/1/21.
 */
public interface CapabilityProviderPart extends MultiPart, ICapabilityProvider {

    boolean hasCapabilities(@Nullable Direction dir);
}
