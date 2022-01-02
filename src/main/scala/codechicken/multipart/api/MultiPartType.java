package codechicken.multipart.api;

import codechicken.lib.data.MCDataInput;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.render.PartRenderer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by covers1624 on 3/16/20.
 */
public abstract class MultiPartType<T extends TMultiPart> extends ForgeRegistryEntry<MultiPartType<?>> {

    // Internal.
    @OnlyIn (Dist.CLIENT)
    PartRenderer<?> renderer;

    public MultiPartType() {
    }

    /**
     * Called to create a {@link TMultiPart} instance on the server
     * side from a {@link CompoundNBT} tag. This is called when
     * the MultiPart is loaded from disk.
     *
     * @param tag The {@link CompoundNBT} to load from.
     * @return The {@link TMultiPart} instance, or null to
     * discard.
     */
    @Nullable
    public abstract T createPartServer(CompoundNBT tag);

    /**
     * Called to create a {@link TMultiPart} instance from
     *
     * @param packet
     * @return
     */
    @Nonnull
    public abstract T createPartClient(MCDataInput packet);
}
