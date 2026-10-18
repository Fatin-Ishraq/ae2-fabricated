/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.guide;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/**
 * FABRIC: Replaces the few GuideME rendering helpers (SimpleRenderContext) that upstream AE2 screens use.
 */
public final class GuiRenderHelper {
    /**
     * Color used for error text drawn over terminals (GuideME SymbolicColor.ERROR_TEXT in upstream).
     */
    public static final int ERROR_TEXT_COLOR = 0xFFFF5555;

    private GuiRenderHelper() {
    }

    public static void fillRect(GuiGraphicsExtractor guiGraphics, LytRect rect, int argb) {
        guiGraphics.fill(rect.x(), rect.y(), rect.right(), rect.bottom(), argb);
    }

    /**
     * Renders text centered in the given rectangle, wrapping lines that do not fit.
     */
    public static void renderTextCenteredIn(GuiGraphicsExtractor guiGraphics, String text, int color, LytRect rect) {
        var font = Minecraft.getInstance().font;
        var lines = font.split(net.minecraft.network.chat.Component.literal(text), Math.max(1, rect.width()));
        var totalHeight = lines.size() * font.lineHeight;
        var y = rect.y() + (rect.height() - totalHeight) / 2;
        for (var line : lines) {
            var x = rect.x() + (rect.width() - font.width(line)) / 2;
            guiGraphics.text(font, line, x, y, color, true);
            y += font.lineHeight;
        }
    }

    public static void renderItem(GuiGraphicsExtractor guiGraphics, ItemStack stack, int x, int y, int width, int height) {
        var pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(width / 16f, height / 16f);
        guiGraphics.item(stack, 0, 0);
        pose.popMatrix();
    }
}
