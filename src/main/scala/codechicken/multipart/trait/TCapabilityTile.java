package codechicken.multipart.trait;

import codechicken.multipart.api.ICapabilityProviderPart;
import codechicken.multipart.api.annotation.MultiPartTrait;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.capability.ChainedProvider;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Created by covers1624 on 7/1/21.
 */
@MultiPartTrait (ICapabilityProviderPart.class)
public class TCapabilityTile extends TileMultiPart {

    //null and all sides.
    private static final Direction[] N_SIDES = Util.make(new Direction[7], arr -> {
        arr[0] = null;
        System.arraycopy(Direction.values(), 0, arr, 1, 6);
    });

    private ChainedProvider[] providers = new ChainedProvider[7];

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        ChainedProvider p = providers[ordinal(side)];
        if (p != null) {
            return p.getCapability(cap, side);
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void copyFrom(TileMultiPart that) {
        super.copyFrom(that);
        if (that instanceof TCapabilityTile) {
            TCapabilityTile thatCapTile = (TCapabilityTile) that;
            providers = thatCapTile.providers;
        }
    }

    @Override
    public void bindPart(TMultiPart _part) {
        super.partAdded(_part);
        if (!(_part instanceof ICapabilityProvider)) {
            return;
        }

        if (!(_part instanceof ICapabilityProviderPart)) {
            throw new IllegalArgumentException("Part " + _part.getType().getRegistryName() + " must implement ICapabilityProviderPart to provide capabilities.");
        }
        ICapabilityProviderPart part = (ICapabilityProviderPart) _part;

        for (Direction side : N_SIDES) {
            if (!part.hasCapabilities(side)) continue;

            int s = ordinal(side);
            if (providers[s] == null) {
                providers[s] = new ChainedProvider(part);
                continue;
            }

            if (side != null && getSlottedPart(s) == part) {
                providers[s].invalidateAll();//We are inserting at the front, all capabilities _may_ be invalid.
                providers[s] = new ChainedProvider(part, providers[s]);
                continue;
            }
            providers[s].append(part);
        }
    }

    @Override
    public void partRemoved(TMultiPart _part, int p) {
        super.partRemoved(_part, p);
        if (!(_part instanceof ICapabilityProviderPart)) {
            return;
        }
        ICapabilityProviderPart part = (ICapabilityProviderPart) _part;

        for (Direction side : N_SIDES) {
            int s = ordinal(side);
            if (providers[s] == null) continue;

            providers[s] = providers[s].remove(part);
        }
    }

    @Override
    public void clearParts() {
        super.clearParts();
        invalidateAll();
        for (Direction side : N_SIDES) {
            int s = ordinal(side);
            providers[s] = null;
        }
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        invalidateAll();
    }

    @Override
    public void internalPartChange(TMultiPart part) {
        super.internalPartChange(part);
        if (!(part instanceof ICapabilityProviderPart)) {
            return;
        }

        invalidateFor((ICapabilityProviderPart) part);
    }

    @Override
    public void multiPartChange(Collection<TMultiPart> parts) {
        super.multiPartChange(parts);

        for (TMultiPart part : parts) {
            if (!(part instanceof ICapabilityProviderPart)) continue;
            invalidateFor((ICapabilityProviderPart) part);
        }
    }

    private void invalidateFor(ICapabilityProviderPart part) {
        for (Direction side : N_SIDES) {
            int s = ordinal(side);
            if (providers[s] == null) continue;

            providers[s].invalidateFor(part);
        }
    }

    private void invalidateAll() {
        for (Direction side : N_SIDES) {
            int s = ordinal(side);
            if (providers[s] != null) {
                providers[s].invalidateAll();
            }
        }
    }

    private static int ordinal(Direction side) {
        return side == null ? 6 : side.ordinal();
    }
}
