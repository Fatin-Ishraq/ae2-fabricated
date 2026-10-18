/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.guide;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

/**
 * FABRIC: Stand-in for GuideME's PageAnchor (GuideME is only available for NeoForge). Points at a page of the AE2
 * guide and optionally an anchor within it.
 */
public record PageAnchor(ResourceLocation pageId, @Nullable String anchor) {
    public static PageAnchor page(ResourceLocation pageId) {
        return new PageAnchor(pageId, null);
    }
}
