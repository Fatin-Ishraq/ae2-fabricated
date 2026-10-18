package appeng.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;

import net.minecraft.core.RegistryAccess;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.MinecraftServer;

/**
 * FABRIC: Stand-in for NeoForge's test framework extension of the same name. Instead of starting a server, it
 * bootstraps Minecraft and AE2 ({@link BootstrapMinecraftExtension}) and injects a mocked {@link MinecraftServer}
 * whose registry access is backed by the vanilla built-in registries (enough for the registry lookups the tests do).
 */
public class EphemeralTestServerProvider implements BeforeAllCallback, ParameterResolver {
    private static MinecraftServer server;

    @Override
    public void beforeAll(ExtensionContext context) {
        BootstrapMinecraftExtension.ensureBootstrapped();
    }

    public static synchronized MinecraftServer grabServer() {
        BootstrapMinecraftExtension.ensureBootstrapped();
        if (server == null) {
            var lookup = VanillaRegistries.createWorldLookup();
            var registryAccess = mock(RegistryAccess.Frozen.class);
            // Registries are views of the vanilla lookups (enough for holder lookups by key)
            when(registryAccess.lookupOrThrow(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
                net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<?>> key = invocation
                        .getArgument(0);
                return mock(net.minecraft.core.Registry.class, Mockito.withSettings()
                        .defaultAnswer(AdditionalAnswers.delegatesTo(lookup.lookupOrThrow(key))));
            });
            server = mock(MinecraftServer.class);
            when(server.registryAccess()).thenReturn(registryAccess);
        }
        return server;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == MinecraftServer.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return grabServer();
    }
}
