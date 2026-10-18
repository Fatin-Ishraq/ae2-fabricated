package appeng.mixins.tests;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;

import appeng.server.testworld.GameTestPlotAdapter;

/**
 * FABRIC: Adds AE2's test plots to the dynamic test instance registry (NeoForge has RegisterGameTestsEvent). Uses the
 * same hook as Fabric API's game test module. Only active when AE2 tests or the Fabric game test runner are enabled.
 */
@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {
    @Unique
    private static final AtomicBoolean AE2_LOADING_TEST_INSTANCES = new AtomicBoolean(false);

    @Unique
    private static boolean ae2$testsEnabled() {
        return Boolean.getBoolean("appeng.tests") || System.getProperty("fabric-api.gametest") != null;
    }

    @Inject(method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/List;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"))
    private static void ae2$loadFromResources(ResourceManager resourceManager,
            List<HolderLookup.RegistryLookup<?>> registries, List<RegistryDataLoader.RegistryData<?>> entries,
            Executor executor, CallbackInfoReturnable<RegistryAccess.Frozen> cir) {
        AE2_LOADING_TEST_INSTANCES.set(entries.stream().anyMatch(entry -> entry.key() == Registries.TEST_INSTANCE));
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "lambda$load$2(Ljava/util/List;Ljava/util/Map;Ljava/lang/Void;)Lnet/minecraft/core/RegistryAccess$Frozen;", at = @At("HEAD"))
    private static void ae2$beforeFreeze(List<RegistryLoadTask<?>> loadTasks, Map<ResourceKey<?>, Exception> loadingErrors,
            Void ignored, CallbackInfoReturnable<RegistryAccess.Frozen> cir) {
        if (!AE2_LOADING_TEST_INSTANCES.getAndSet(false) || !ae2$testsEnabled()) {
            return;
        }
        for (var task : loadTasks) {
            if (task.registry.key() == Registries.TEST_INSTANCE) {
                var testInstances = (Registry<GameTestInstance>) task.registry;
                GameTestPlotAdapter.registerAll((id, instance) -> Registry.register(testInstances, id, instance));
            }
        }
    }
}
