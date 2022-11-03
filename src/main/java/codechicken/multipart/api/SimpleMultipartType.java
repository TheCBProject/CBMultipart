package codechicken.multipart.api;

import codechicken.lib.data.MCDataInput;
import codechicken.multipart.api.part.MultiPart;
import net.minecraft.nbt.CompoundTag;

/**
 * A simple implementation of {@link MultipartType} providing
 * a unified callback to create the part for each side.
 * <p>
 * Created by covers1624 on 3/17/20.
 */
public class SimpleMultipartType<T extends MultiPart> extends MultipartType<T> {

    private final SimpleMultiPartTypeFactory<T> factory;

    public SimpleMultipartType(SimpleMultiPartTypeFactory<T> factory) {
        this.factory = factory;
    }

    @Override
    public T createPartServer(CompoundTag tag) {
        return factory.create(false);
    }

    @Override
    public T createPartClient(MCDataInput packet) {
        return factory.create(true);
    }

    public interface SimpleMultiPartTypeFactory<T extends MultiPart> {

        T create(boolean client);
    }
}
