package codechicken.microblock.util;

import codechicken.lib.vec.Cuboid6;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A cuboid and side mask pair.
 * <p>
 * Cuboids should be treated as immutable and not modified.
 * <p>
 * Masks are, if bit set, side is important.
 * <p>
 * {@link MaskedCuboid}s should be created with {@link MaskedCuboid#of(Cuboid6, int)} to receive automatic interning and caching.
 * Making sure these are interned is important, as these are stored inside caches as keys, and may be long-lived. Ensuring that
 * duplicates aren't saved is important to keep memory usage down. Ideally we should cache sets of cuboids as well, but that's a later issue.
 * <p>
 * Created by covers1624 on 20/10/22.
 */
public record MaskedCuboid(Cuboid6 box, int sideMask) {

    private static final Cache<MaskedCuboid, MaskedCuboid> CUBOID_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    public static MaskedCuboid intern(MaskedCuboid cuboid) {
        return CUBOID_CACHE.asMap().computeIfAbsent(cuboid, Function.identity());
    }

    public static MaskedCuboid of(Cuboid6 box, int sideMask) {
        return intern(new MaskedCuboid(box.copy(), sideMask));
    }
}
