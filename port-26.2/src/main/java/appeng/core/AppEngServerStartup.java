/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.core;

import net.fabricmc.api.DedicatedServerModInitializer;

/**
 * FABRIC: Entrypoint for dedicated servers. Replaces {@code @Mod(dist = Dist.DEDICATED_SERVER)} on AppEngServer.
 */
public class AppEngServerStartup implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        new AppEngServer();
    }
}
