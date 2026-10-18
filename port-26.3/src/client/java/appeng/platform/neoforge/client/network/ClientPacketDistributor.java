package appeng.platform.neoforge.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * FABRIC: Sends payloads from the client to the server through Fabric networking.
 */
public final class ClientPacketDistributor {
    private ClientPacketDistributor() {
    }

    public static void sendToServer(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        ClientPlayNetworking.send(payload);
        for (var other : payloads) {
            ClientPlayNetworking.send(other);
        }
    }
}
