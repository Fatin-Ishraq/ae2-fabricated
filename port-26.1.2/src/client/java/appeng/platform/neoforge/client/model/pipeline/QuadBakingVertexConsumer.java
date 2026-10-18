package appeng.platform.neoforge.client.model.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

import appeng.platform.neoforge.client.model.quad.MutableQuad;

/**
 * FABRIC: A vertex consumer that collects exactly 4 vertices and bakes them into a quad (through {@link MutableQuad}).
 */
public class QuadBakingVertexConsumer implements VertexConsumer {
    private final MutableQuad quad = new MutableQuad();
    private int vertexIndex = -1;

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        vertexIndex++;
        if (vertexIndex >= 4) {
            throw new IllegalStateException("A quad has only 4 vertices");
        }
        quad.setPosition(vertexIndex, x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        quad.setColor(vertexIndex, r, g, b, a);
        return this;
    }

    @Override
    public VertexConsumer setColor(int packedColor) {
        quad.setColor(vertexIndex, packedColor);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        quad.setUv(vertexIndex, u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        quad.setNormal(vertexIndex, x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float lineWidth) {
        return this;
    }

    public void setTintIndex(int tintIndex) {
        quad.setTintIndex(tintIndex);
    }

    public void setDirection(Direction direction) {
        quad.setDirection(direction);
    }

    public void setSprite(Material.Baked material) {
        quad.setSprite(material);
    }

    public void setShade(boolean shade) {
        quad.setShade(shade);
    }

    public void setLightEmission(int lightEmission) {
        quad.setLightEmission(lightEmission);
    }

    public void setAmbientOcclusion(boolean ambientOcclusion) {
        quad.setAmbientOcclusion(ambientOcclusion);
    }

    public BakedQuad bakeQuad() {
        if (vertexIndex != 3) {
            throw new IllegalStateException("Not enough vertices for a quad: " + (vertexIndex + 1));
        }
        var result = quad.toBakedQuad();
        quad.reset();
        vertexIndex = -1;
        return result;
    }
}
