package appeng.platform.neoforge.network.handling;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * FABRIC: Handles a received payload.
 */
@FunctionalInterface
public interface IPayloadHandler<T extends CustomPacketPayload> {
    void handle(T payload, IPayloadContext context);
}
