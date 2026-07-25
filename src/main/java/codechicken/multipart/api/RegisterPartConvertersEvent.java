package codechicken.multipart.api;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.List;

/**
 * Created by covers1624 on 7/26/26.
 */
public final class RegisterPartConvertersEvent extends Event implements IModBusEvent {

    private final List<PartConverter> converters;

    public RegisterPartConvertersEvent(List<PartConverter> converters) {
        this.converters = converters;
    }

    public void register(PartConverter converter) {
        converters.add(converter);
    }
}
