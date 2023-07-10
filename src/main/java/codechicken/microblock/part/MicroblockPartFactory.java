package codechicken.microblock.part;

import codechicken.lib.data.MCDataInput;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.util.MicroMaterialRegistry;
import codechicken.multipart.api.MultipartType;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 26/6/22.
 */
public abstract class MicroblockPartFactory extends MultipartType<MicroblockPart> {

    public abstract MicroblockPart create(boolean client, MicroMaterial material);

    @Nullable
    @Override
    public MicroblockPart createPartServer(CompoundTag tag) {
        MicroMaterial material = MicroMaterialRegistry.getMaterial(tag.getString("material"));
        if (material == null) return null;

        return create(false, material);
    }

    @NotNull
    @Override
    public MicroblockPart createPartClient(MCDataInput packet) {
        return create(true, packet.readRegistryIdDirect(MicroMaterialRegistry.MICRO_MATERIALS));
    }

    public abstract float getResistanceFactor();
}
