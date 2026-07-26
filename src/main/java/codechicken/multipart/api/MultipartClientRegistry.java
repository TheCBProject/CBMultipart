package codechicken.multipart.api;

import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.render.DynamicPartRenderer;
import codechicken.multipart.api.part.render.OutlinePartRenderer;
import codechicken.multipart.api.part.render.PartParticleHandler;
import codechicken.multipart.api.part.render.StaticPartRenderer;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import org.jspecify.annotations.Nullable;

import static codechicken.multipart.CBMultipart.MOD_ID;
import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 8/11/21.
 */
public class MultipartClientRegistry {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    public static void init(IEventBus modBus) {
        LOCK.lock();
        modBus.addListener(MultipartClientRegistry::registerReloadListeners);
    }

    private static void registerReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(MOD_ID, "part_renderers"), new PartRendererReloadListener());
    }

    @Nullable
    public static <T extends MultiPart> StaticPartRenderer<T> getStaticPartRenderer(MultipartType<?> type) {
        return unsafeCast(type.staticRenderer);
    }

    @Nullable
    public static <T extends MultiPart, S> DynamicPartRenderer<T, S> getDynamicPartRenderer(MultipartType<?> type) {
        return unsafeCast(type.dynamicRenderer);
    }

    @Nullable
    public static <T extends MultiPart> OutlinePartRenderer<T> getOutlinePartRenderer(MultipartType<?> type) {
        return unsafeCast(type.outlineRenderer);
    }

    @Nullable
    public static <T extends MultiPart> PartParticleHandler<T> getPartParticleHandler(MultipartType<?> type) {
        return unsafeCast(type.particleHandler);
    }

    public static void loadStaticPartRenderers() {
        for (MultipartType<?> type : MultipartType.REGISTRY) {
            type.staticRenderer = null;

            if (type.staticRendererFactory instanceof StaticPartRenderer.Factory<?> factory) {
                type.staticRenderer = factory.create();
            }
        }
    }

    private static void loadDynamicPartRenderers() {
        for (MultipartType<?> type : MultipartType.REGISTRY) {
            type.dynamicRenderer = null;

            if (type.dynamicRendererFactory instanceof DynamicPartRenderer.Factory<?, ?> factory) {
                type.dynamicRenderer = factory.create();
            }
        }
    }

    private static class PartRendererReloadListener implements ResourceManagerReloadListener {

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            loadStaticPartRenderers();
            loadDynamicPartRenderers();
        }
    }
}
