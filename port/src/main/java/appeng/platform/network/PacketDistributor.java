/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.network;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

/**
 * FABRIC: Same static API as NeoForge's PacketDistributor, implemented on top of Fabric networking.
 */
public final class PacketDistributor {
    private PacketDistributor() {
    }

    /**
     * Client only.
     */
    public static void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload payload) {
        for (var player : PlayerLookup.world(level)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer excluded, double x, double y,
            double z, double radius, CustomPacketPayload payload) {
        var radiusSq = radius * radius;
        for (var player : level.players()) {
            if (player == excluded) {
                continue;
            }
            if (player.distanceToSqr(x, y, z) < radiusSq) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    public static void sendToAllPlayers(net.minecraft.server.MinecraftServer server, CustomPacketPayload payload) {
        for (var player : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos, CustomPacketPayload payload) {
        for (var player : PlayerLookup.tracking(level, chunkPos)) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
