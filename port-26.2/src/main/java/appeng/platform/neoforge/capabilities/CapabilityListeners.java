package appeng.platform.neoforge.capabilities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * FABRIC: NeoForge-style capability invalidation listeners. NeoForge notifies listeners registered for a position
 * whenever the capabilities at that position may have changed. On Fabric they are notified when the block at the
 * position changes ({@code appeng.mixins.platform.ServerLevelMixin}), when a block entity loads or unloads there, and
 * when {@code invalidateCapabilities} is called. Listeners are held weakly; a listener returning false is removed.
 */
public final class CapabilityListeners {
    private static final Map<ServerLevel, Long2ObjectOpenHashMap<List<WeakReference<ICapabilityInvalidationListener>>>> LISTENERS = new WeakHashMap<>();

    private CapabilityListeners() {
    }

    /**
     * Called once by AppEngBase.
     */
    public static void init() {
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((be, level) -> invalidate(level, be.getBlockPos()));
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((be, level) -> invalidate(level, be.getBlockPos()));
    }

    public static synchronized void register(ServerLevel level, BlockPos pos, ICapabilityInvalidationListener listener) {
        LISTENERS.computeIfAbsent(level, l -> new Long2ObjectOpenHashMap<>())
                .computeIfAbsent(pos.asLong(), p -> new ArrayList<>())
                .add(new WeakReference<>(listener));
    }

    public static void invalidate(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        List<WeakReference<ICapabilityInvalidationListener>> listeners;
        synchronized (CapabilityListeners.class) {
            var byPos = LISTENERS.get(serverLevel);
            if (byPos == null) {
                return;
            }
            listeners = byPos.remove(pos.asLong());
            if (listeners == null) {
                return;
            }
        }
        // Call outside the lock; listeners may register new listeners
        var kept = new ArrayList<WeakReference<ICapabilityInvalidationListener>>();
        for (var ref : listeners) {
            var listener = ref.get();
            if (listener != null && listener.onInvalidate()) {
                kept.add(ref);
            }
        }
        if (!kept.isEmpty()) {
            synchronized (CapabilityListeners.class) {
                LISTENERS.computeIfAbsent(serverLevel, l -> new Long2ObjectOpenHashMap<>())
                        .computeIfAbsent(pos.asLong(), p -> new ArrayList<>())
                        .addAll(kept);
            }
        }
    }
}
