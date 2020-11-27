package codechicken.multipart.api;

import codechicken.lib.data.MCDataInput;
import codechicken.multipart.api.MultiPartType;
import codechicken.multipart.api.part.TMultiPart;
import net.minecraft.nbt.CompoundNBT;

/**
 * A simple implementation of {@link MultiPartType} providing
 * a unified callback to create the part for each side.
 *
 * Created by covers1624 on 3/17/20.
 */
public class SimpleMultiPartType<T extends TMultiPart> extends MultiPartType<T> {

    private final SimpleMultiPartTypeFactory<T> factory;

    public SimpleMultiPartType(SimpleMultiPartTypeFactory<T> factory) {
        this.factory = factory;
    }

    @Override
    public T createPartServer(CompoundNBT tag) {
        return factory.create(false);
    }

    @Override
    public T createPartClient(MCDataInput packet) {
        return factory.create(true);
    }

    public interface SimpleMultiPartTypeFactory<T extends TMultiPart> {

        T create(boolean client);
    }
}
