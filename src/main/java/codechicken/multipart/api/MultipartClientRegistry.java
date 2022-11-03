package codechicken.multipart.api;

import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.render.PartRenderer;

import javax.annotation.Nullable;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 8/11/21.
 */
public class MultipartClientRegistry {

    /**
     * Register a {@link PartRenderer} for a given {@link MultipartType}.
     *
     * @param type     The {@link MultipartType}.
     * @param renderer The {@link PartRenderer}.
     * @throws IllegalArgumentException When attempting to replace an already registered {@link PartRenderer}
     */
    public static synchronized <T extends MultiPart> void register(MultipartType<? super T> type, PartRenderer<? super T> renderer) {
        if (type.renderer != null) {
            throw new IllegalArgumentException(
                    "Attempted to replace part renderer for: "
                            + type.getRegistryType()
                            + ". Prev: " + type.renderer.getClass()
                            + ", New: " + renderer.getClass()
            );
        }
        type.renderer = renderer;
    }

    /**
     * Get the {@link PartRenderer} for the given {@link MultipartType}.
     *
     * @param type The {@link MultipartType} to get the renderer for.
     * @return The {@link PartRenderer}, or <code>null</code> if none exists.
     */
    @Nullable
    public static <T extends MultiPart> PartRenderer<T> getRenderer(MultipartType<?> type) {
        return unsafeCast(type.renderer);
    }
}
