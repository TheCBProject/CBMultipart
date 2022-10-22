package codechicken.microblock.factory;

import codechicken.lib.data.MCDataInput;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.part.MicroblockPart;
import codechicken.microblock.util.MicroMaterialRegistry;
import codechicken.multipart.api.MultiPartType;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 26/6/22.
 */
public abstract class MicroblockPartFactory extends MultiPartType<MicroblockPart> {

    public abstract MicroblockPart create(boolean client, MicroMaterial material);

    @Nullable
    @Override
    public MicroblockPart createPartServer(CompoundTag tag) {
        return create(false, MicroMaterialRegistry.getMaterial(tag.getString("material")));
    }

    @NotNull
    @Override
    public MicroblockPart createPartClient(MCDataInput packet) {
        return create(true, packet.readRegistryIdUnsafe(MicroMaterialRegistry.MICRO_MATERIALS));
    }

    public abstract float getResistanceFactor();
}
