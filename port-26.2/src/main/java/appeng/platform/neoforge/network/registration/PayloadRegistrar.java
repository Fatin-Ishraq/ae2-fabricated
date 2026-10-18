package appeng.platform.neoforge.network.registration;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import appeng.platform.neoforge.network.handling.IPayloadContext;
import appeng.platform.neoforge.network.handling.IPayloadHandler;

/**
 * FABRIC: Registers play payload types with Fabric networking. Client-side handlers are registered separately by the
 * client (see {@code RegisterClientPayloadHandlersEvent}).
 */
public final class PayloadRegistrar {
    public <T extends CustomPacketPayload> PayloadRegistrar playToClient(CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.clientboundPlay().register(type, codec);
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar playToServer(CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        PayloadTypeRegistry.serverboundPlay().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type,
                (payload, context) -> handler.handle(payload, IPayloadContext.of(context.player())));
        return this;
    }

    /**
     * Registers the payload in both directions; the handler only handles payloads received by the server.
     */
    public <T extends CustomPacketPayload> PayloadRegistrar playBidirectional(CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> serverHandler) {
        playToClient(type, codec);
        return playToServer(type, codec, serverHandler);
    }
}
