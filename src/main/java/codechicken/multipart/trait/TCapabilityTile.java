package codechicken.multipart.trait;

import codechicken.multipart.api.part.CapabilityProviderPart;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.capability.ChainedProvider;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by covers1624 on 7/1/21.
 */
public class TCapabilityTile extends TileMultipart {

    //null and all sides.
    private static final Direction[] N_SIDES = Util.make(new Direction[7], arr -> {
        arr[0] = null;
        System.arraycopy(Direction.values(), 0, arr, 1, 6);
    });

    private ChainedProvider[] providers = new ChainedProvider[7];

    public TCapabilityTile(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        ChainedProvider p = providers[ordinal(side)];
        if (p != null) {
            return p.getCapability(cap, side);
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void copyFrom(TileMultipart that) {
        super.copyFrom(that);
        if (that instanceof TCapabilityTile thatCapTile) {
            providers = thatCapTile.providers;
        }
    }

    @Override
    public void bindPart(MultiPart _part) {
        super.bindPart(_part);
        if (!(_part instanceof CapabilityProviderPart part)) return;

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
    public void partRemoved(MultiPart _part, int p) {
        super.partRemoved(_part, p);
        if (!(_part instanceof CapabilityProviderPart part)) {
            return;
        }

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
    public void internalPartChange(@Nullable MultiPart part) {
        super.internalPartChange(part);
        if (!(part instanceof CapabilityProviderPart)) {
            return;
        }

        invalidateFor((CapabilityProviderPart) part);
    }

    @Override
    public void multiPartChange(Collection<MultiPart> parts) {
        super.multiPartChange(parts);

        for (MultiPart part : parts) {
            if (!(part instanceof CapabilityProviderPart)) continue;
            invalidateFor((CapabilityProviderPart) part);
        }
    }

    private void invalidateFor(CapabilityProviderPart part) {
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

    private static int ordinal(@Nullable Direction side) {
        return side == null ? 6 : side.ordinal();
    }
}
