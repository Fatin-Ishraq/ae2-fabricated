package appeng.platform.neoforge.network.handling;

import java.util.concurrent.CompletableFuture;

import net.minecraft.world.entity.player.Player;

/**
 * FABRIC: Context of a received payload. Fabric runs payload handlers on the game thread already, so
 * {@link #enqueueWork} runs the task immediately.
 */
public interface IPayloadContext {
    Player player();

    default CompletableFuture<Void> enqueueWork(Runnable task) {
        task.run();
        return CompletableFuture.completedFuture(null);
    }

    static IPayloadContext of(Player player) {
        return () -> player;
    }
}
