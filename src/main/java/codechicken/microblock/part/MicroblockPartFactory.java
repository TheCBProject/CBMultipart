package codechicken.microblock.part;

import codechicken.microblock.api.MicroMaterial;
import codechicken.multipart.api.MultipartType;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
        var material = input.read("material", MicroMaterial.CODEC).orElse(null);
        if (material == null) return null;

        return create(false, material);
    }

    @Override
    public MicroblockPart createPartClient(RegistryFriendlyByteBuf packet) {
        return create(true, packet.cc$readWithRegistryCodec(MicroMaterial.STREAM_CODEC));
    }

    public abstract float getResistanceFactor();
}
