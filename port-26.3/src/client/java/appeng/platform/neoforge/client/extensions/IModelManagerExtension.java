package appeng.platform.neoforge.client.extensions;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.model.loading.v1.FabricModelManager;

import appeng.platform.neoforge.client.model.standalone.StandaloneModelKey;

/**
 * FABRIC: NeoForge's {@code ModelManager#getStandaloneModel}, backed by Fabric extra models. Injected through
 * ae2.classtweaker and {@code appeng.mixins.platform.client.ModelManagerMixin}.
 */
public interface IModelManagerExtension {
    @Nullable
    default <T> T getStandaloneModel(StandaloneModelKey<T> key) {
        return ((FabricModelManager) this).getModel(key.fabricKey());
    }
}
