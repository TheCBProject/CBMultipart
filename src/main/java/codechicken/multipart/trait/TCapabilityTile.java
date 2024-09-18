package codechicken.multipart.trait;

import codechicken.multipart.api.part.CapabilityProviderPart;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 7/1/21.
 */
public class TCapabilityTile extends TileMultipart {

    public TCapabilityTile(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public <T, C> @Nullable T getCapability(BlockCapability<T, C> capability, C context) {
        // If the capability uses a Direction for context, we give slotted parts priority.
        if (capability.contextClass().equals(Direction.class)) {
            @SuppressWarnings ("unchecked")
            T result = getSlottedCapability((BlockCapability<T, Direction>) capability, (Direction) context);
            if (result != null) {
                return result;
            }
        }
        // Otherwise, whoever is in the list first gets it, this may need to be a bit more deterministic.
        for (MultiPart part : getPartList()) {
            if (part instanceof CapabilityProviderPart capPart) {
                T result = capPart.getCapability(capability, context);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    @SuppressWarnings ("DataFlowIssue") // Intellij nullability is unable to pass the nullable through to part.getCapability.
    private <T, C extends @Nullable Direction> @Nullable T getSlottedCapability(BlockCapability<T, C> capability, C context) {
        int slot = context != null ? context.ordinal() : 6;
        if (getSlottedPart(slot) instanceof CapabilityProviderPart part) {
            return part.getCapability(capability, context);
        }
        return null;
    }
}
