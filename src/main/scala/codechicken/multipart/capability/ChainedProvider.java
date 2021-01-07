package codechicken.multipart.capability;

import codechicken.lib.util.ArrayUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Arrays;

/**
 * Created by covers1624 on 7/1/21.
 */
public class ChainedProvider implements ICapabilityProvider {

    public ICapabilityProvider provider;
    public ChainedProvider next;

    private Capability<?>[] seenCaps = new Capability[0];
    private LazyOptional<?>[] wrapped = new LazyOptional[0];

    public ChainedProvider(ICapabilityProvider provider) {
        this(provider, null);
    }

    public ChainedProvider(ICapabilityProvider provider, ChainedProvider next) {
        this.provider = provider;
        this.next = next;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        LazyOptional<?> ret = getExisting(cap);
        if (ret.isPresent()) return ret.cast();

        ret = wrap(cap, provider.getCapability(cap, side));
        if (ret.isPresent()) return ret.cast();

        return next != null ? next.getCapability(cap, side) : LazyOptional.empty();
    }

    public void append(ICapabilityProvider p) {
        if (next != null) {
            next.append(p);
        } else {
            next = new ChainedProvider(p);
        }
    }

    public ChainedProvider remove(ICapabilityProvider p) {
        if (provider == p) {
            invalidate(null);
            return next;
        }
        if (next != null) {
            return next.remove(p);
        }
        return this;
    }

    public void invalidateFor(ICapabilityProvider p) {
        if (provider == p) {
            invalidate(null);
            return;
        }
        if(next != null) {
            next.invalidateFor(p);
        }
    }

    public void invalidate(Capability<?> cap) {
        if (cap == null) {
            for (int i = 0; i < seenCaps.length; i++) {
                seenCaps[i] = null;
                wrapped[i].invalidate();
                wrapped[i] = null;
            }
        } else {
            int idx = ArrayUtils.indexOfRef(seenCaps, cap);
            seenCaps[idx] = null;
            wrapped[idx].invalidate();
            wrapped[idx] = null;
        }
    }

    public void invalidateAll() {
        invalidate(null);
        if (next != null) {
            next.invalidateAll();
        }
    }

    private LazyOptional<?> wrap(Capability<?> cap, LazyOptional<?> opt) {
        if (!opt.isPresent()) return LazyOptional.empty();

        int idx = ArrayUtils.indexOfRef(seenCaps, null);
        if (idx == -1) {
            idx = seenCaps.length;
            seenCaps = Arrays.copyOf(seenCaps, idx + 1);
            wrapped = Arrays.copyOf(wrapped, idx + 1);
        }
        seenCaps[idx] = cap;
        wrapped[idx] = LazyOptional.of(() -> opt.orElseThrow(() -> new RuntimeException("Present but missing LazyOptional?")));
        opt.addListener(e -> invalidate(cap));
        return wrapped[idx];
    }

    private LazyOptional<?> getExisting(Capability<?> cap) {
        int idx = ArrayUtils.indexOfRef(seenCaps, cap);
        if (idx == -1) {
            return LazyOptional.empty();
        }
        return wrapped[idx];
    }
}