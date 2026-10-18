package appeng.api.stacks;

import java.util.HashSet;
import java.util.Set;
import com.google.common.base.Preconditions;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;

/**
 * Manages the registry used to synchronize key spaces to the client.
 */
@ApiStatus.Internal
public final class AEKeyTypesInternal {
    @Nullable
    private static Registry<AEKeyType> registry;

    @Nullable
    private static Set<AEKeyType> allTypes;

    private AEKeyTypesInternal() {
    }

    public static Registry<AEKeyType> getRegistry() {
        Preconditions.checkState(registry != null, "AE2 isn't initialized yet.");
        return registry;
    }

    public static void setRegistry(Registry<AEKeyType> registry) {
        Preconditions.checkState(AEKeyTypesInternal.registry == null);
        AEKeyTypesInternal.registry = registry;
    }

    // FABRIC: NeoForge rebuilds this set from a registry bake callback. Fabric has no equivalent, so the set is
    // rebuilt lazily whenever the registry size changes (key types are only ever added during mod init).
    public static Set<AEKeyType> getAllTypes() {
        var registry = getRegistry();
        var types = allTypes;
        if (types == null || types.size() != registry.size()) {
            var newTypes = new HashSet<AEKeyType>();
            for (var aeKeyType : registry) {
                newTypes.add(aeKeyType);
            }
            allTypes = types = Set.copyOf(newTypes);
        }
        return types;
    }

    public static void register(AEKeyType keyType) {
        Registry.register(getRegistry(), keyType.getId(), keyType);
    }
}
