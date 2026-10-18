package appeng.platform.neoforge.client.network.event;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import appeng.platform.neoforge.network.handling.IPayloadContext;
import appeng.platform.neoforge.network.handling.IPayloadHandler;

/**
 * FABRIC: Registers client-side payload handlers with Fabric networking.
 */
public class RegisterClientPayloadHandlersEvent {
    public <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, IPayloadHandler<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type,
                (payload, context) -> handler.handle(payload, IPayloadContext.of(context.player())));
    }
}
