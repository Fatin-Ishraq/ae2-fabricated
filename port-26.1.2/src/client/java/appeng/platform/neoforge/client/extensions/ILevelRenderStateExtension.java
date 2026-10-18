package appeng.platform.neoforge.client.extensions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.util.context.ContextKey;

/**
 * FABRIC: NeoForge's custom data on the level render state, stored in Fabric render state data. Injected through
 * ae2.classtweaker and {@code appeng.mixins.platform.client.LevelRenderStateMixin}.
 */
public interface ILevelRenderStateExtension {
    Map<ContextKey<?>, RenderStateDataKey<?>> KEYS = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private static <T> RenderStateDataKey<T> fabricKey(ContextKey<T> key) {
        return (RenderStateDataKey<T>) KEYS.computeIfAbsent(key,
                k -> RenderStateDataKey.create(() -> k.name().toString()));
    }

    default <T> void setRenderData(ContextKey<T> key, @Nullable T value) {
        ((FabricRenderState) this).setData(fabricKey(key), value);
    }

    @Nullable
    default <T> T getRenderData(ContextKey<T> key) {
        return ((FabricRenderState) this).getData(fabricKey(key));
    }

    default <T> T getRenderDataOrDefault(ContextKey<T> key, T defaultValue) {
        return ((FabricRenderState) this).getDataOrDefault(fabricKey(key), defaultValue);
    }
}
