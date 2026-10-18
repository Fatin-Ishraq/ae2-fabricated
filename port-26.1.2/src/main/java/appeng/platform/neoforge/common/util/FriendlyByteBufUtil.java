package appeng.platform.neoforge.common.util;

import java.util.function.Consumer;

import io.netty.buffer.Unpooled;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * FABRIC: Serializes data written to a registry-aware buffer into a byte array.
 */
public final class FriendlyByteBufUtil {
    private FriendlyByteBufUtil() {
    }

    public static byte[] writeCustomData(Consumer<RegistryFriendlyByteBuf> writer, RegistryAccess registryAccess) {
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
        try {
            writer.accept(buf);
            var result = new byte[buf.readableBytes()];
            buf.readBytes(result);
            return result;
        } finally {
            buf.release();
        }
    }
}
