package codechicken.multipart.client;

import codechicken.multipart.init.CBMultipartModContent;
import codechicken.multipart.util.ProxyBlockAndTintGetter;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.model.data.ModelData;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Registry for exposing tint indicies from a multipart block.
 * <p>
 * Created by covers1624 on 7/26/26.
 */
public class MultipartTintRegistry {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    private static final AtomicInteger next = new AtomicInteger();

    private static final ReadWriteLock passthroughRwLock = new ReentrantReadWriteLock();
    private static final Int2ObjectMap<ProxyTint> passthroughTint = new Int2ObjectOpenHashMap<>();
    private static final Object2IntMap<ProxyTint> passthroughTintInverse = new Object2IntOpenHashMap<>();

    public static void init(IEventBus modBus) {
        LOCK.lock();

        modBus.addListener(MultipartTintRegistry::onRegisterBlockColorHandlers);
    }

    /**
     * Register (or get) a tint index to proxy through to the given state.
     *
     * @param state     The state to proxy to.
     * @param tintIndex The tint index.
     * @return The tint index to expose via your static renderer.
     */
    public static int getOrRegisterPassthroughTint(BlockState state, int tintIndex) {
        var key = new ProxyTint(state, tintIndex);
        passthroughRwLock.readLock().lock();
        try {
            var ourTintIndex = passthroughTintInverse.getOrDefault(key, -1);
            if (ourTintIndex != -1) return ourTintIndex;
        } finally {
            passthroughRwLock.readLock().unlock();
        }

        passthroughRwLock.writeLock().lock();
        try {
            int ourTintIndex = next.getAndIncrement();
            passthroughTintInverse.put(key, ourTintIndex);
            passthroughTint.put(ourTintIndex, key);
            return ourTintIndex;
        } finally {
            passthroughRwLock.writeLock().unlock();
        }
    }

    private static void onRegisterBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        event.register(new MultipartBlockColor(event.getBlockColors()), CBMultipartModContent.MULTIPART_BLOCK.get());
    }

    private static @Nullable ProxyTint getProxyTint(int tintIndex) {
        passthroughRwLock.readLock().lock();
        try {
            return passthroughTint.get(tintIndex);
        } finally {
            passthroughRwLock.readLock().unlock();
        }
    }

    private record ProxyTint(BlockState state, int tintIndex) { }

    private record MultipartBlockColor(BlockColors blockColors) implements BlockColor {

        @Override
        public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
            var proxyTint = getProxyTint(tintIndex);
            if (proxyTint != null) {
                return blockColors.getColor(
                        proxyTint.state,
                        level != null && pos != null ? new ProxyBlockAndTintGetter(level, pos, proxyTint.state, ModelData.EMPTY) : null,
                        pos,
                        proxyTint.tintIndex
                );
            }
            return -1;
        }
    }
}
