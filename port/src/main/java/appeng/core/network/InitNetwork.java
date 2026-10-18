package appeng.core.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import appeng.core.AppEng;
import appeng.core.network.bidirectional.ConfigValuePacket;
import appeng.core.network.clientbound.AssemblerAnimationPacket;
import appeng.core.network.clientbound.BlockTransitionEffectPacket;
import appeng.core.network.clientbound.ClearPatternAccessTerminalPacket;
import appeng.core.network.clientbound.CompassResponsePacket;
import appeng.core.network.clientbound.CraftConfirmPlanPacket;
import appeng.core.network.clientbound.CraftingJobStatusPacket;
import appeng.core.network.clientbound.CraftingStatusPacket;
import appeng.core.network.clientbound.ExportedGridContent;
import appeng.core.network.clientbound.GuiDataSyncPacket;
import appeng.core.network.clientbound.ItemTransitionEffectPacket;
import appeng.core.network.clientbound.LightningPacket;
import appeng.core.network.clientbound.MEInventoryUpdatePacket;
import appeng.core.network.clientbound.MatterCannonPacket;
import appeng.core.network.clientbound.MockExplosionPacket;
import appeng.core.network.clientbound.NetworkStatusPacket;
import appeng.core.network.clientbound.PatternAccessTerminalPacket;
import appeng.core.network.clientbound.SetLinkStatusPacket;
import appeng.core.network.serverbound.ColorApplicatorSelectColorPacket;
import appeng.core.network.serverbound.ConfigButtonPacket;
import appeng.core.network.serverbound.ConfirmAutoCraftPacket;
import appeng.core.network.serverbound.FillCraftingGridFromRecipePacket;
import appeng.core.network.serverbound.GuiActionPacket;
import appeng.core.network.serverbound.HotkeyPacket;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.core.network.serverbound.MEInteractionPacket;
import appeng.core.network.serverbound.MouseWheelPacket;
import appeng.core.network.serverbound.PartLeftClickPacket;
import appeng.core.network.serverbound.QuickMovePatternPacket;
import appeng.core.network.serverbound.RequestClosestMeteoritePacket;
import appeng.core.network.serverbound.SelectKeyTypePacket;
import appeng.core.network.serverbound.SwapSlotsPacket;
import appeng.core.network.serverbound.SwitchGuisPacket;
import appeng.core.network.serverbound.UpdateHoldingCtrlPacket;

public class InitNetwork {
    // FABRIC: Payload types are registered with PayloadTypeRegistry; server receivers are registered here and client
    // receivers in initClient(), which is only called from the client entrypoint.
    private static final java.util.List<Runnable> CLIENT_RECEIVERS = new java.util.ArrayList<>();

    public static void init() {
        var registrar = new PayloadRegistrar();

        // Clientbound
        clientbound(registrar, AssemblerAnimationPacket.TYPE, AssemblerAnimationPacket.STREAM_CODEC);
        clientbound(registrar, BlockTransitionEffectPacket.TYPE, BlockTransitionEffectPacket.STREAM_CODEC);
        clientbound(registrar, ClearPatternAccessTerminalPacket.TYPE, ClearPatternAccessTerminalPacket.STREAM_CODEC);
        clientbound(registrar, CompassResponsePacket.TYPE, CompassResponsePacket.STREAM_CODEC);
        clientbound(registrar, CraftConfirmPlanPacket.TYPE, CraftConfirmPlanPacket.STREAM_CODEC);
        clientbound(registrar, CraftingJobStatusPacket.TYPE, CraftingJobStatusPacket.STREAM_CODEC);
        clientbound(registrar, CraftingStatusPacket.TYPE, CraftingStatusPacket.STREAM_CODEC);
        clientbound(registrar, GuiDataSyncPacket.TYPE, GuiDataSyncPacket.STREAM_CODEC);
        clientbound(registrar, ItemTransitionEffectPacket.TYPE, ItemTransitionEffectPacket.STREAM_CODEC);
        clientbound(registrar, LightningPacket.TYPE, LightningPacket.STREAM_CODEC);
        clientbound(registrar, MatterCannonPacket.TYPE, MatterCannonPacket.STREAM_CODEC);
        clientbound(registrar, MEInventoryUpdatePacket.TYPE, MEInventoryUpdatePacket.STREAM_CODEC);
        clientbound(registrar, MockExplosionPacket.TYPE, MockExplosionPacket.STREAM_CODEC);
        clientbound(registrar, NetworkStatusPacket.TYPE, NetworkStatusPacket.STREAM_CODEC);
        clientbound(registrar, PatternAccessTerminalPacket.TYPE, PatternAccessTerminalPacket.STREAM_CODEC);
        clientbound(registrar, SetLinkStatusPacket.TYPE, SetLinkStatusPacket.STREAM_CODEC);
        clientbound(registrar, ExportedGridContent.TYPE, ExportedGridContent.STREAM_CODEC);

        // Serverbound
        serverbound(registrar, ColorApplicatorSelectColorPacket.TYPE, ColorApplicatorSelectColorPacket.STREAM_CODEC);
        serverbound(registrar, RequestClosestMeteoritePacket.TYPE, RequestClosestMeteoritePacket.STREAM_CODEC);
        serverbound(registrar, ConfigButtonPacket.TYPE, ConfigButtonPacket.STREAM_CODEC);
        serverbound(registrar, ConfirmAutoCraftPacket.TYPE, ConfirmAutoCraftPacket.STREAM_CODEC);
        serverbound(registrar, FillCraftingGridFromRecipePacket.TYPE, FillCraftingGridFromRecipePacket.STREAM_CODEC);
        serverbound(registrar, GuiActionPacket.TYPE, GuiActionPacket.STREAM_CODEC);
        serverbound(registrar, HotkeyPacket.TYPE, HotkeyPacket.STREAM_CODEC);
        serverbound(registrar, InventoryActionPacket.TYPE, InventoryActionPacket.STREAM_CODEC);
        serverbound(registrar, MEInteractionPacket.TYPE, MEInteractionPacket.STREAM_CODEC);
        serverbound(registrar, MouseWheelPacket.TYPE, MouseWheelPacket.STREAM_CODEC);
        serverbound(registrar, PartLeftClickPacket.TYPE, PartLeftClickPacket.STREAM_CODEC);
        serverbound(registrar, QuickMovePatternPacket.TYPE, QuickMovePatternPacket.STREAM_CODEC);
        serverbound(registrar, SelectKeyTypePacket.TYPE, SelectKeyTypePacket.STREAM_CODEC);
        serverbound(registrar, SwapSlotsPacket.TYPE, SwapSlotsPacket.STREAM_CODEC);
        serverbound(registrar, SwitchGuisPacket.TYPE, SwitchGuisPacket.STREAM_CODEC);
        serverbound(registrar, UpdateHoldingCtrlPacket.TYPE, UpdateHoldingCtrlPacket.STREAM_CODEC);

        // Bidirectional
        bidirectional(registrar, ConfigValuePacket.TYPE, ConfigValuePacket.STREAM_CODEC);
    }

    /**
     * Registers the client-side receivers. Must only be called on the physical client.
     */
    public static void initClient() {
        CLIENT_RECEIVERS.forEach(Runnable::run);
    }

    private static final class PayloadRegistrar {
    }

    private static <T extends ClientboundPacket> void clientbound(PayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.playS2C().register(type, codec);
        CLIENT_RECEIVERS.add(() -> ClientReceivers.register(type));
    }

    private static <T extends ServerboundPacket> void serverbound(PayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.playC2S().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> payload.handleOnServer(context.player()));
    }

    private static <T extends ServerboundPacket & ClientboundPacket> void bidirectional(PayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.playS2C().register(type, codec);
        PayloadTypeRegistry.playC2S().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> payload.handleOnServer(context.player()));
        CLIENT_RECEIVERS.add(() -> ClientReceivers.register(type));
    }

    /**
     * Separate class so that client-only networking classes are only loaded on the client.
     */
    private static final class ClientReceivers {
        static <T extends ClientboundPacket> void register(CustomPacketPayload.Type<T> type) {
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(type,
                    (payload, context) -> payload.handleOnClient(context.player()));
        }
    }
}
