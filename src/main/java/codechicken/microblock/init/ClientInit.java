package codechicken.microblock.init;

import codechicken.microblock.client.MicroblockItemRenderer;
import codechicken.microblock.client.MicroblockPartRenderer;
import codechicken.microblock.client.MicroblockRender;
import codechicken.microblock.part.MicroblockPart;
import codechicken.microblock.part.MicroblockPartParticleHandler;
import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.RegisterMultipartRenderersEvent;
import net.covers1624.quack.util.CrashLock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

/**
 * Created by covers1624 on 20/10/22.
 */
public class ClientInit {

    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    public static void init(IEventBus modBus) {
        LOCK.lock();

        modBus.addListener(ClientInit::onRegisterMultipartRenderers);
        modBus.addListener(ClientInit::onRegisterSpecialModelRenderers);

        MicroblockRender.init(modBus);
    }

    private static void onRegisterMultipartRenderers(RegisterMultipartRenderersEvent event) {
        registerRenderers(event, CBMicroblockModContent.FACE_MICROBLOCK_PART.get());
        registerRenderers(event, CBMicroblockModContent.HOLLOW_MICROBLOCK_PART.get());
        registerRenderers(event, CBMicroblockModContent.CORNER_MICROBLOCK_PART.get());
        registerRenderers(event, CBMicroblockModContent.EDGE_MICROBLOCK_PART.get());
        registerRenderers(event, CBMicroblockModContent.POST_MICROBLOCK_PART.get());
    }

    private static void registerRenderers(RegisterMultipartRenderersEvent event, MultipartType<? extends MicroblockPart> type) {
        event.registerStaticPartRenderer(type, MicroblockPartRenderer::new);
        event.registerParticleHandler(type, new MicroblockPartParticleHandler());
    }

    private static void onRegisterSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(MicroblockItemRenderer.Unbaked.TYPE, MicroblockItemRenderer.Unbaked.CODEC);
    }
}
