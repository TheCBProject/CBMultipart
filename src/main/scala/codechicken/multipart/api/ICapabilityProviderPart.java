package codechicken.multipart.api;

import codechicken.multipart.api.annotation.MultiPartMarker;
import codechicken.multipart.trait.TCapabilityTile;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

/**
 * Created by covers1624 on 7/1/21.
 */
@MultiPartMarker (TCapabilityTile.class)
public interface ICapabilityProviderPart extends ICapabilityProvider {

    boolean hasCapabilities(Direction dir);
}
