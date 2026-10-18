package appeng.util;

import java.util.function.Consumer;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

/**
 * FABRIC: Tests run under fabric-loader-junit. Bootstraps Minecraft and runs the main and dedicated server
 * entrypoints of all mods once, which registers AE2's content (NeoForge does this through its test framework).
 */
public class BootstrapMinecraftExtension implements Extension, BeforeAllCallback {
    private static boolean modInitialized;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
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
        }
    }

    private static <T> void invokeEntrypoints(String name, Class<T> type, Consumer<? super T> invoker) {
        for (var container : FabricLoaderImpl.INSTANCE.getEntrypointContainers(name, type)) {
            invoker.accept(container.getEntrypoint());
        }
    }
}
