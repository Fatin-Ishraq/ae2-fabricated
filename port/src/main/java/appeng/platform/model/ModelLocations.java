/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.model;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * FABRIC: Replaces NeoForge's ModelResourceLocation.standalone(id). Fabric bakes additional models (registered with
 * ModelLoadingPlugin.Context#addModels) under the "fabric_resource" variant, so the same lookup through the model
 * manager works.
 */
public final class ModelLocations {
    private static final String STANDALONE_VARIANT = "fabric_resource";

    private ModelLocations() {
    }

    public static ModelResourceLocation standalone(ResourceLocation id) {
        return new ModelResourceLocation(id, STANDALONE_VARIANT);
    }
}
