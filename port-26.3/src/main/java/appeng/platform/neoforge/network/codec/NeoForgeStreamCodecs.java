/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.neoforge.network.codec;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;

/**
 * FABRIC: The subset of NeoForge's NeoForgeStreamCodecs that AE2 uses.
 */
public final class NeoForgeStreamCodecs {
    private NeoForgeStreamCodecs() {
    }

    public static final StreamCodec<ByteBuf, ChunkPos> CHUNK_POS = new StreamCodec<>() {
        @Override
        public ChunkPos decode(ByteBuf buf) {
            return ChunkPos.unpack(buf.readLong());
        }

        @Override
        public void encode(ByteBuf buf, ChunkPos pos) {
            buf.writeLong(pos.pack());
        }
    };

    public static <T extends Enum<T>> StreamCodec<FriendlyByteBuf, T> enumCodec(Class<T> enumClass) {
        return new StreamCodec<>() {
            @Override
            public T decode(FriendlyByteBuf buf) {
                return buf.readEnum(enumClass);
            }

            @Override
            public void encode(FriendlyByteBuf buf, T value) {
                buf.writeEnum(value);
            }
        };
    }
}
