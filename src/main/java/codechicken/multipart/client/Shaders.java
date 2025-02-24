package codechicken.multipart.client;

import codechicken.lib.render.shader.CCShaderInstance;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static codechicken.multipart.CBMultipart.MOD_ID;

/**
 * Created by covers1624 on 23/1/24.
 */
public class Shaders {

    public static final CrashLock LOCK = new CrashLock("Already Initialised");

    private static @Nullable CCShaderInstance highlightShader;

    public static void init(IEventBus modBus) {
        LOCK.lock();
        modBus.addListener(Shaders::onRegisterShaders);
    }

    private static void onRegisterShaders(RegisterShadersEvent event) {
        event.registerShader(
                CCShaderInstance.create(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(MOD_ID, "highlight"), DefaultVertexFormat.BLOCK),
                e -> highlightShader = (CCShaderInstance) e
        );
    }

    public static CCShaderInstance highlightShader() {
        return Objects.requireNonNull(highlightShader, "Highlight shader not loaded yet.");
    }
}
