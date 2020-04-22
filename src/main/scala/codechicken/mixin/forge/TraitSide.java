package codechicken.mixin.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Created by covers1624 on 4/13/20.
 */
public enum TraitSide {
    COMMON(Dist.CLIENT, Dist.DEDICATED_SERVER),
    SERVER(Dist.DEDICATED_SERVER),
    CLIENT(Dist.CLIENT);

    private final EnumSet<Dist> dists;

    TraitSide(Dist... dists) {
        this.dists = EnumSet.copyOf(Arrays.asList(dists));
    }

    public boolean isSupported() {
        return dists.contains(FMLEnvironment.dist);
    }

    public boolean isClient() {
        return this == CLIENT;
    }

    public boolean isServer() {
        return this == SERVER;
    }

    public boolean isCommon() {
        return this == COMMON;
    }

}
