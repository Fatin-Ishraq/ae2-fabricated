package appeng.platform.neoforge.client.model;

import org.joml.Matrix4fc;

import com.mojang.math.Transformation;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.core.Direction;

/**
 * FABRIC: A model state that applies an extra transformation after the one of its parent.
 */
public record ComposedModelState(ModelState parent, Transformation transformation) implements ModelState {
    public ComposedModelState(ModelState parent, Transformation transformation) {
        this.parent = parent;
        this.transformation = parent.transformation().compose(transformation);
    }

    @Override
    public Matrix4fc faceTransformation(Direction side) {
        return parent.faceTransformation(side);
    }

    @Override
    public Matrix4fc inverseFaceTransformation(Direction side) {
        return parent.inverseFaceTransformation(side);
    }
}
