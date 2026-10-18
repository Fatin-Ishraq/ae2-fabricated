package appeng.core.network;

import net.minecraft.world.entity.player.Player;

/**
 * FABRIC: Fabric API invokes play payload handlers on the client thread, so no work enqueueing is needed.
 */
public interface ClientboundPacket extends CustomAppEngPayload {
    default void handleOnClient(Player player) {
        throw new AbstractMethodError("Unimplemented method on " + getClass());
    }
}
