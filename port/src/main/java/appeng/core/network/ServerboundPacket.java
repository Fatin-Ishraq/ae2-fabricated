package appeng.core.network;

import net.minecraft.server.level.ServerPlayer;

/**
 * FABRIC: Fabric API invokes play payload handlers on the server thread, so no work enqueueing is needed.
 */
public interface ServerboundPacket extends CustomAppEngPayload {
    void handleOnServer(ServerPlayer player);
}
