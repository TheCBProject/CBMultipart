package codechicken.microblock.part;

import codechicken.lib.data.MCDataInput;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.util.MicroMaterialRegistry;
import codechicken.multipart.api.MultipartType;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 26/6/22.
 */
public abstract class MicroblockPartFactory extends MultipartType<MicroblockPart> {

    public abstract MicroblockPart create(boolean client, MicroMaterial material);

    @Nullable
    @Override
    public MicroblockPart createPartServer(ValueInput input) {
        MicroMaterial material = MicroMaterialRegistry.getMaterial(input.getString("material").orElseThrow()); // TODO codec read?
        if (material == null) return null;

        return create(false, material);
    }

    @Override
    public MicroblockPart createPartClient(MCDataInput packet) {
        return create(true, packet.readRegistryIdDirect(MicroMaterialRegistry.microMaterials()));
    }

    public abstract float getResistanceFactor();
}
