/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * FABRIC: Entrypoint for clients. Replaces {@code @Mod(dist = Dist.CLIENT)} on AppEngClient.
 */
public class AppEngClientStartup implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new AppEngClient();
    }
}
