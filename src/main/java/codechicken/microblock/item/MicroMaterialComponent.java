package codechicken.microblock.item;

import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.part.StandardMicroFactory;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 23/10/24.
 */
public record MicroMaterialComponent(
        int factoryId,
        int size,
        MicroMaterial material
) {

    public static final Codec<MicroMaterialComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    Codec.INT.fieldOf("factoryId").forGetter(MicroMaterialComponent::factoryId),
                    Codec.INT.fieldOf("size").forGetter(MicroMaterialComponent::size),
                    MicroMaterial.CODEC.fieldOf("material").forGetter(MicroMaterialComponent::material)
            ).apply(builder, MicroMaterialComponent::new)
    );

    public static StreamCodec<RegistryFriendlyByteBuf, MicroMaterialComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MicroMaterialComponent::factoryId,
            ByteBufCodecs.VAR_INT, MicroMaterialComponent::size,
            MicroMaterial.STREAM_CODEC, MicroMaterialComponent::material,
            MicroMaterialComponent::new
    );

    public MicroMaterialComponent {
        if (factoryId == -1) throw new IllegalArgumentException("Illegal factory id.");
    }

    @Contract (pure = true)
    public @Nullable StandardMicroFactory factory() {
        return StandardMicroFactory.FACTORIES.get(factoryId);
    }

    public static @Nullable MicroMaterialComponent getComponent(ItemStack stack) {
        return stack.get(CBMicroblockModContent.MICRO_MATERIAL_COMPONENT);
    }
}
