/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.guide;

/**
 * FABRIC: Stand-in for GuideME's LytRect, a simple integer rectangle used by some AE2 screens.
 */
public record LytRect(int x, int y, int width, int height) {
    private static final LytRect EMPTY = new LytRect(0, 0, 0, 0);

    public static LytRect empty() {
        return EMPTY;
    }

    public int right() {
        return x + width;
    }

    public int bottom() {
        return y + height;
    }
}
