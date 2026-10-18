package appeng.util;

import java.util.function.Consumer;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;

/**
 * FABRIC: Tests run under fabric-loader-junit. Bootstraps Minecraft and runs the main and dedicated server
 * entrypoints of all mods once, which registers AE2's content (NeoForge starts an ephemeral server for this).
 */
public class BootstrapMinecraftExtension implements Extension, BeforeAllCallback {
    private static boolean modInitialized;

    @Override
    public void beforeAll(ExtensionContext context) {
        ensureBootstrapped();
    }

    public static synchronized void ensureBootstrapped() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        if (!modInitialized) {
            modInitialized = true;
            invokeEntrypoints("main", ModInitializer.class, ModInitializer::onInitialize);
            invokeEntrypoints("server", DedicatedServerModInitializer.class,
                    DedicatedServerModInitializer::onInitializeServer);
            // Item and other data components are bound when a server loads its registries
            BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(VanillaRegistries.createLookup())
                    .forEach(DataComponentInitializers.PendingComponents::apply);
        }
    }

    private static <T> void invokeEntrypoints(String name, Class<T> type, Consumer<? super T> invoker) {
        for (var container : FabricLoaderImpl.INSTANCE.getEntrypointContainers(name, type)) {
            invoker.accept(container.getEntrypoint());
        }
    }
}
