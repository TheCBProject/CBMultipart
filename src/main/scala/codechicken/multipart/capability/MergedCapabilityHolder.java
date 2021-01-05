package codechicken.multipart.capability;

import codechicken.lib.util.ArrayUtils;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.TileMultiPart;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by covers1624 on 3/1/21.
 */
public class MergedCapabilityHolder implements ICapabilityProvider {

    private static final int INITIAL_ARRAY_SIZE = 4;
    private static final float LOAD_FACTOR = 1.5F;

    private final TileMultiPart tile;

    private final SideInstance genericCache = new SideInstance(null);
    private final EnumMap<Direction, SideInstance> sidedCache = new EnumMap<>(Direction.class);

    public MergedCapabilityHolder(TileMultiPart tile) {
        this.tile = tile;
        for (Direction side : Direction.BY_INDEX) {
            sidedCache.put(side, new SideInstance(side));
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (side != null) {
            return sidedCache.get(side).getCapability(cap);
        }
        return genericCache.getCapability(cap);
    }

    public void invalidate() {
        genericCache.invalidate();
        for (Direction side : Direction.BY_INDEX) {
            sidedCache.get(side).invalidate();
        }
    }

    private class SideInstance {

        private final Set<Capability<?>> negativeCache = new HashSet<>();
        private final Direction side;

        private Capability<?>[] caps = ArrayUtils.newArray(Capability.class, INITIAL_ARRAY_SIZE);
        private LazyOptional<?>[] merged = ArrayUtils.newArray(LazyOptional.class, INITIAL_ARRAY_SIZE);

        private SideInstance(Direction side) {
            this.side = side;
        }

        public <T> LazyOptional<T> getCapability(Capability<T> cap) {
            int idx = ArrayUtils.indexOfRef(caps, cap);
            if (idx == -1) {
                if (negativeCache.contains(cap)) {
                    return LazyOptional.empty();
                }
                return compute(cap);
            }

            return merged[idx].cast();
        }

        public void invalidate() {
            negativeCache.clear();
            ArrayUtils.fill(caps, null);
            for (LazyOptional<?> opt : merged) {
                if (opt != null) {
                    opt.invalidate();
                }
            }
            Arrays.fill(merged, null);
        }

        public void invalidateCap(Capability<?> cap) {
            int idx = ArrayUtils.indexOfRef(caps, cap);
            if (idx == -1) return;
            caps[idx] = null;
            merged[idx].invalidate();
        }

        private <T> LazyOptional<T> compute(Capability<T> cap) {
            int idx = ArrayUtils.indexOfRef(caps, null);
            if (idx == -1) {
                resize();
                idx = ArrayUtils.indexOfRef(caps, null);
            }
            List<T> list = new ArrayList<>();
            for (TMultiPart part : tile.getPartList()) {
                if (part instanceof ICapabilityProvider) {
                    ICapabilityProvider provider = (ICapabilityProvider) part;
                    LazyOptional<T> opt = provider.getCapability(cap, side);
                    if (opt.isPresent()) {
                        list.add(opt.orElseThrow(() -> new RuntimeException("LazyOptional is present but returned null.")));
                        opt.addListener(e -> invalidateCap(cap));
                    }
                }
            }
            if (list.isEmpty()) {
                negativeCache.add(cap);
                return LazyOptional.empty();
            }
            caps[idx] = cap;
            LazyOptional<T> m = LazyOptional.of(() -> CapabilityMerger.merge(cap, list));
            merged[idx] = m;
            return m;
        }

        private void resize() {
            caps = Arrays.copyOf(caps, (int) Math.ceil(caps.length * LOAD_FACTOR));
            merged = Arrays.copyOf(merged, (int) Math.ceil(merged.length * LOAD_FACTOR));
        }
    }

}
