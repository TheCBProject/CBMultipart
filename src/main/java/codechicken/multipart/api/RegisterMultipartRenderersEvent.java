package codechicken.multipart.api;

import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.render.DynamicPartRenderer;
import codechicken.multipart.api.part.render.OutlinePartRenderer;
import codechicken.multipart.api.part.render.PartParticleHandler;
import codechicken.multipart.api.part.render.StaticPartRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Created by covers1624 on 7/26/26.
 */
public final class RegisterMultipartRenderersEvent extends Event implements IModBusEvent {

    /**
     * Register the given part types renderer for static geometry.
     * <p>
     * This is equivalent to {@link BlockStateModel}.
     *
     * @param type    The part type.
     * @param factory The factory.
     */
    public <P extends MultiPart> void registerStaticPartRenderer(MultipartType<? extends P> type, StaticPartRenderer.Factory<P> factory) {
        if (type.staticRendererFactory != null) {
            throw new IllegalArgumentException("Can't replace part renderer for type " + type);
        }

        type.staticRendererFactory = factory;
    }

    /**
     * Register the given part types renderer for dynamic geometry.
     * <p>
     * This is equivalent to {@link BlockEntityRenderer}.
     *
     * @param type    The part type.
     * @param factory The factory.
     */
    public <P extends MultiPart, S> void registerDynamicPartRenderer(MultipartType<? extends P> type, DynamicPartRenderer.Factory<P, S> factory) {
        if (type.dynamicRendererFactory != null) {
            throw new IllegalArgumentException("Can't replace part renderer for type " + type);
        }

        type.dynamicRendererFactory = factory;
    }

    /**
     * Register the given part types renderer for outlines.
     * <p>
     * This can override the built-in whole part shape renderer.
     *
     * @param type     The part type.
     * @param renderer The renderer.
     */
    public <P extends MultiPart> void registerOutlinePartRenderer(MultipartType<? extends P> type, OutlinePartRenderer<P> renderer) {
        if (type.outlineRenderer != null) {
            throw new IllegalArgumentException("Can't replace part renderer for type " + type);
        }

        type.outlineRenderer = renderer;
    }

    /**
     * Register a particle handler for your part.
     *
     * @param type    The part type.
     * @param handler The particle handler.
     */
    public <P extends MultiPart> void registerParticleHandler(MultipartType<? extends P> type, PartParticleHandler<P> handler) {
        if (type.particleHandler != null) {
            throw new IllegalArgumentException("Can't replace part particle handler for type " + type);
        }

        type.particleHandler = handler;
    }
}
