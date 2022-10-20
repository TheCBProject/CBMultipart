package codechicken.microblock.factory;

import codechicken.lib.data.MCDataInput;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.part.MicroblockPart;
import codechicken.microblock.part.placement.PlacementProperties;
import codechicken.microblock.util.MicroMaterialRegistries;
import codechicken.multipart.api.MultiPartType;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 26/6/22.
 */
public abstract class MicroblockPartFactory<T extends MicroblockPart> extends MultiPartType<T> {

    public abstract MultiPartType<?> getType();

    public abstract T create(boolean client, MicroMaterial material);

    @Nullable
    @Override
    public T createPartServer(CompoundTag tag) {
        return create(false, MicroMaterialRegistries.getMaterial(tag.getString("material")));
    }

    @NotNull
    @Override
    public T createPartClient(MCDataInput packet) {
        return create(true, packet.readRegistryIdUnsafe(MicroMaterialRegistries.MICRO_MATERIALS));
    }

    public abstract PlacementProperties placementProperties();
}
