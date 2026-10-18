package appeng.platform.neoforge.server;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/**
 * FABRIC: Tracks the running server through Fabric's server lifecycle events ({@link #init()} is called by
 * {@code AppEngBase}).
 */
public final class ServerLifecycleHooks {
    @Nullable
    private static MinecraftServer currentServer;

    private ServerLifecycleHooks() {
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
        // Forget the server only after all other listeners (including AE2's own shutdown) have run
        var late = net.minecraft.resources.Identifier.fromNamespaceAndPath("ae2", "late");
        ServerLifecycleEvents.SERVER_STOPPED.addPhaseOrdering(net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE, late);
        ServerLifecycleEvents.SERVER_STOPPED.register(late, server -> {
            if (currentServer == server) {
                currentServer = null;
            }
        });
    }

    @Nullable
    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }
}
