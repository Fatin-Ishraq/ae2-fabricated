package appeng.platform.neoforge.client.model.quad;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import com.mojang.math.Transformation;

import net.minecraft.client.resources.model.geometry.BakedQuad;

/**
 * FABRIC: Applies a transformation to the vertex positions of a baked quad, keeping its {@link QuadExtras}.
 */
public final class QuadTransforms {
    private QuadTransforms() {
    }

    public static BakedQuad applyTransformation(BakedQuad quad, Transformation transformation) {
        if (transformation.equals(Transformation.IDENTITY)) {
            return quad;
        }
        var result = new BakedQuad(
                transform(quad.position0(), transformation),
                transform(quad.position1(), transformation),
                transform(quad.position2(), transformation),
                transform(quad.position3(), transformation),
                quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
                quad.direction(),
                quad.materialInfo());
        return QuadExtras.with(result, QuadExtras.colors(quad), QuadExtras.ambientOcclusion(quad));
    }

    private static Vector3fc transform(Vector3fc position, Transformation transformation) {
        var v = new Vector4f(position.x(), position.y(), position.z(), 1);
        transformation.getMatrix().transform(v);
        v.div(v.w);
        return new Vector3f(v.x, v.y, v.z);
    }
}
