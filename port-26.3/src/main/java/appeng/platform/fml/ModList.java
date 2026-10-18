package appeng.platform.fml;

import net.fabricmc.loader.api.FabricLoader;

/**
 * FABRIC: Mod presence checks through Fabric Loader.
 */
public final class ModList {
    private static final ModList INSTANCE = new ModList();

    private ModList() {
    }

    public static ModList get() {
        return INSTANCE;
    }

    public boolean isLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
