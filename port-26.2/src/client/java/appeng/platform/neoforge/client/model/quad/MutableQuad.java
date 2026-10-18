package appeng.platform.neoforge.client.model.quad;

import java.util.Arrays;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.mojang.blaze3d.platform.Transparency;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

/**
 * FABRIC: A quad that can be edited vertex by vertex and then baked into a vanilla {@link BakedQuad}. Texture
 * coordinates are stored in atlas space, like in baked quads. Vertex colors and the ambient occlusion flag are kept on
 * the baked quad through {@link QuadExtras}. Vertex normals are accepted but not stored, since vanilla derives normals
 * from the quad geometry.
 */
public final class MutableQuad {
    private static final int WHITE = 0xFFFFFFFF;

    private final Vector3f[] positions = { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
    private final long[] uvs = new long[4];
    private final int[] colors = new int[4];
    private Direction direction = Direction.DOWN;
    private TextureAtlasSprite sprite;
    private ChunkSectionLayer chunkLayer;
    private RenderType itemRenderType;
    private int tintIndex;
    private boolean shade;
    private int lightEmission;
    private boolean ambientOcclusion;

    public MutableQuad() {
        reset();
    }

    public MutableQuad reset() {
        for (var position : positions) {
            position.zero();
        }
        Arrays.fill(uvs, 0L);
        Arrays.fill(colors, WHITE);
        direction = Direction.DOWN;
        sprite = null;
        chunkLayer = null;
        itemRenderType = null;
        tintIndex = -1;
        shade = true;
        lightEmission = 0;
        ambientOcclusion = true;
        return this;
    }

    // Positions

    public float x(int vertex) {
        return positions[vertex].x;
    }

    public float y(int vertex) {
        return positions[vertex].y;
    }

    public float z(int vertex) {
        return positions[vertex].z;
    }

    public float positionComponent(int vertex, int component) {
        return positions[vertex].get(component);
    }

    public Vector3f copyPosition(int vertex) {
        return new Vector3f(positions[vertex]);
    }

    public Vector3f copyPosition(int vertex, Vector3f dest) {
        return dest.set(positions[vertex]);
    }

    public MutableQuad setX(int vertex, float x) {
        positions[vertex].x = x;
        return this;
    }

    public MutableQuad setY(int vertex, float y) {
        positions[vertex].y = y;
        return this;
    }

    public MutableQuad setZ(int vertex, float z) {
        positions[vertex].z = z;
        return this;
    }

    public MutableQuad setPositionComponent(int vertex, int component, float value) {
        positions[vertex].setComponent(component, value);
        return this;
    }

    public MutableQuad setPosition(int vertex, float x, float y, float z) {
        positions[vertex].set(x, y, z);
        return this;
    }

    public MutableQuad setPosition(int vertex, Vector3fc position) {
        positions[vertex].set(position);
        return this;
    }

    /**
     * Places the quad on a block side, in the coordinate system of a sprite as it is shown on that side in vanilla
     * block models (for UP, the top of the sprite points north; for DOWN, it points south). All values are in 0..1;
     * {@code depth} is the distance from the block side towards the block center.
     */
    public MutableQuad setCubeFaceFromSpriteCoords(Direction side, float left, float bottom, float right, float top,
            float depth) {
        this.direction = side;
        // Vertex order: top-left, bottom-left, bottom-right, top-right (counter-clockwise when facing the side)
        float[] us = { left, left, right, right };
        float[] vs = { top, bottom, bottom, top };
        for (int i = 0; i < 4; i++) {
            float a = us[i];
            float b = vs[i];
            switch (side) {
                case NORTH -> positions[i].set(1 - a, b, depth);
                case SOUTH -> positions[i].set(a, b, 1 - depth);
                case EAST -> positions[i].set(1 - depth, b, 1 - a);
                case WEST -> positions[i].set(depth, b, a);
                case UP -> positions[i].set(a, 1 - depth, 1 - b);
                case DOWN -> positions[i].set(a, depth, b);
            }
        }
        return this;
    }

    // Texture coordinates

    public float u(int vertex) {
        return UVPair.unpackU(uvs[vertex]);
    }

    public float v(int vertex) {
        return UVPair.unpackV(uvs[vertex]);
    }

    public long packedUv(int vertex) {
        return uvs[vertex];
    }

    public MutableQuad setUv(int vertex, float u, float v) {
        uvs[vertex] = UVPair.pack(u, v);
        return this;
    }

    public MutableQuad setPackedUv(int vertex, long packedUv) {
        uvs[vertex] = packedUv;
        return this;
    }

    public MutableQuad setUvFromSprite(int vertex, float u, float v) {
        return setUv(vertex, requiredSprite().getU(u), requiredSprite().getV(v));
    }

    public MutableQuad bakeUvsFromPosition() {
        return bakeUvsFromPosition(UVTransform.IDENTITY);
    }

    /**
     * Derives texture coordinates by projecting the vertices onto the side the quad faces, like vanilla does for
     * model elements without explicit UVs.
     */
    public MutableQuad bakeUvsFromPosition(UVTransform transform) {
        var s = requiredSprite();
        for (int i = 0; i < 4; i++) {
            var p = positions[i];
            long spriteUv = switch (direction) {
                case DOWN -> UVPair.pack(p.x, 1 - p.z);
                case UP -> UVPair.pack(p.x, p.z);
                case NORTH -> UVPair.pack(1 - p.x, 1 - p.y);
                case SOUTH -> UVPair.pack(p.x, 1 - p.y);
                case WEST -> UVPair.pack(p.z, 1 - p.y);
                case EAST -> UVPair.pack(1 - p.z, 1 - p.y);
            };
            if (!transform.isIdentity()) {
                spriteUv = transform.transformPacked(spriteUv);
            }
            uvs[i] = UVPair.pack(s.getU(UVPair.unpackU(spriteUv)), s.getV(UVPair.unpackV(spriteUv)));
        }
        return this;
    }

    // Material

    public int tintIndex() {
        return tintIndex;
    }

    public MutableQuad setTintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
        return this;
    }

    public Direction direction() {
        return direction;
    }

    public MutableQuad setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    public TextureAtlasSprite sprite() {
        return sprite;
    }

    public TextureAtlasSprite requiredSprite() {
        if (sprite == null) {
            throw new IllegalStateException("No sprite set on quad");
        }
        return sprite;
    }

    public ChunkSectionLayer chunkLayer() {
        return chunkLayer;
    }

    public RenderType itemRenderType() {
        return itemRenderType;
    }

    public MutableQuad setSprite(Material.Baked material) {
        var transparency = material.sprite().contents().transparency();
        if (material.forceTranslucent()) {
            transparency = transparency.or(Transparency.TRANSLUCENT);
        }
        return setSprite(material, transparency);
    }

    public MutableQuad setSprite(Material.Baked material, Transparency transparency) {
        // Let vanilla pick the chunk layer and item render type for the material
        var info = BakedQuad.MaterialInfo.of(material, transparency, tintIndex, shade, lightEmission);
        return setSprite(info.sprite(), info.layer(), info.itemRenderType());
    }

    public MutableQuad setSprite(TextureAtlasSprite sprite, ChunkSectionLayer chunkLayer, RenderType itemRenderType) {
        this.sprite = sprite;
        this.chunkLayer = chunkLayer;
        this.itemRenderType = itemRenderType;
        return this;
    }

    public boolean shade() {
        return shade;
    }

    public MutableQuad setShade(boolean shade) {
        this.shade = shade;
        return this;
    }

    public int lightEmission() {
        return lightEmission;
    }

    public MutableQuad setLightEmission(int lightEmission) {
        this.lightEmission = lightEmission;
        return this;
    }

    public boolean hasAmbientOcclusion() {
        return ambientOcclusion;
    }

    public MutableQuad setAmbientOcclusion(boolean ambientOcclusion) {
        this.ambientOcclusion = ambientOcclusion;
        return this;
    }

    // Colors and normals

    public int color(int vertex) {
        return colors[vertex];
    }

    public MutableQuad setColor(int argb) {
        Arrays.fill(colors, argb);
        return this;
    }

    public MutableQuad setColor(int vertex, int argb) {
        colors[vertex] = argb;
        return this;
    }

    public MutableQuad setColor(int vertex, int r, int g, int b, int a) {
        return setColor(vertex, (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF);
    }

    /**
     * Accepted for API compatibility; vanilla quads derive their normal from the geometry.
     */
    public MutableQuad setNormal(int vertex, float x, float y, float z) {
        return this;
    }

    // Conversion

    public MutableQuad setFrom(BakedQuad quad) {
        for (int i = 0; i < 4; i++) {
            positions[i].set(quad.position(i));
            uvs[i] = quad.packedUV(i);
        }
        direction = quad.direction();
        var info = quad.materialInfo();
        sprite = info.sprite();
        chunkLayer = info.layer();
        itemRenderType = info.itemRenderType();
        tintIndex = info.tintIndex();
        shade = info.shade();
        lightEmission = info.lightEmission();
        var quadColors = QuadExtras.colors(quad);
        if (quadColors != null) {
            System.arraycopy(quadColors, 0, colors, 0, 4);
        } else {
            Arrays.fill(colors, WHITE);
        }
        ambientOcclusion = QuadExtras.ambientOcclusion(quad);
        return this;
    }

    public BakedQuad toBakedQuad() {
        var info = new BakedQuad.MaterialInfo(requiredSprite(), chunkLayer, itemRenderType, tintIndex, shade,
                lightEmission);
        var quad = new BakedQuad(
                new Vector3f(positions[0]), new Vector3f(positions[1]),
                new Vector3f(positions[2]), new Vector3f(positions[3]),
                uvs[0], uvs[1], uvs[2], uvs[3],
                direction, info);
        boolean white = colors[0] == WHITE && colors[1] == WHITE && colors[2] == WHITE && colors[3] == WHITE;
        return QuadExtras.with(quad, white ? null : colors.clone(), ambientOcclusion);
    }

    public MutableQuad copy() {
        return copyInto(new MutableQuad());
    }

    public MutableQuad copyInto(MutableQuad dest) {
        for (int i = 0; i < 4; i++) {
            dest.positions[i].set(positions[i]);
        }
        System.arraycopy(uvs, 0, dest.uvs, 0, 4);
        System.arraycopy(colors, 0, dest.colors, 0, 4);
        dest.direction = direction;
        dest.sprite = sprite;
        dest.chunkLayer = chunkLayer;
        dest.itemRenderType = itemRenderType;
        dest.tintIndex = tintIndex;
        dest.shade = shade;
        dest.lightEmission = lightEmission;
        dest.ambientOcclusion = ambientOcclusion;
        return dest;
    }
}
