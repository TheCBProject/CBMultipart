package codechicken.microblock.factory;

import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.part.StandardMicroblockPart;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;

/**
 * Created by covers1624 on 9/7/22.
 */
public abstract class StandardMicroFactory extends MicroblockPartFactory {

    private static final Int2ObjectMap<StandardMicroFactory> _FACTORIES = new Int2ObjectArrayMap<>(5);
    public static final Int2ObjectMap<StandardMicroFactory> FACTORIES = Int2ObjectMaps.unmodifiable(_FACTORIES);

    public final int factoryId;

    protected StandardMicroFactory(int factoryId) {
        this.factoryId = factoryId;
        assert !_FACTORIES.containsKey(factoryId) : "Factory with ID already exists.";
        _FACTORIES.put(factoryId, this);
    }

    @Override
    public abstract StandardMicroblockPart create(boolean client, MicroMaterial material);

    public abstract int getItemSlot();
}
