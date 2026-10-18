/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * FABRIC: Minimal replacement for NeoForge's QuadBakingVertexConsumer, covering what AE2 uses: four vertices with
 * position, color, uv and normal, baked into a quad in the vanilla BLOCK vertex format.
 */
public final class QuadBakingVertexConsumer {
    // BLOCK vertex format: position (3 floats), color (int), uv0 (2 floats), uv2 (int), normal (int)
    private static final int STRIDE = 8;

    private final int[] vertices = new int[STRIDE * 4];
    private int vertexIndex = -1;
    private TextureAtlasSprite sprite;
    private Direction direction = Direction.DOWN;
    private int tintIndex = -1;
    private boolean shade = true;

    public QuadBakingVertexConsumer addVertex(float x, float y, float z) {
        vertexIndex++;
        if (vertexIndex >= 4) {
            throw new IllegalStateException("A quad can only have 4 vertices");
        }
        int offset = vertexIndex * STRIDE;
        vertices[offset] = Float.floatToRawIntBits(x);
        vertices[offset + 1] = Float.floatToRawIntBits(y);
        vertices[offset + 2] = Float.floatToRawIntBits(z);
        vertices[offset + 3] = -1; // white
        vertices[offset + 6] = 0; // light, filled in by the renderer
        return this;
    }

    public QuadBakingVertexConsumer setColor(float r, float g, float b, float a) {
        vertices[vertexIndex * STRIDE + 3] = FastColor.ABGR32.color(
                Mth.floor(a * 255), Mth.floor(b * 255), Mth.floor(g * 255), Mth.floor(r * 255));
        return this;
    }

    public QuadBakingVertexConsumer setUv(float u, float v) {
        int offset = vertexIndex * STRIDE;
        vertices[offset + 4] = Float.floatToRawIntBits(u);
        vertices[offset + 5] = Float.floatToRawIntBits(v);
        return this;
    }

    public QuadBakingVertexConsumer setNormal(float x, float y, float z) {
        int packed = (Mth.floor(x * 127) & 0xFF)
                | (Mth.floor(y * 127) & 0xFF) << 8
                | (Mth.floor(z * 127) & 0xFF) << 16;
        vertices[vertexIndex * STRIDE + 7] = packed;
        return this;
    }

    public void setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setTintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
    }

    public void setShade(boolean shade) {
        this.shade = shade;
    }

    public BakedQuad bakeQuad() {
        if (vertexIndex != 3) {
            throw new IllegalStateException("Quad is incomplete: " + (vertexIndex + 1) + " vertices");
        }
        return new BakedQuad(vertices.clone(), tintIndex, direction, sprite, shade);
    }
}
