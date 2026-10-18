package appeng.platform.neoforge.client.model.standalone;

import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedExtraModel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.Identifier;

/**
 * FABRIC: An extra model baked from a single model file. Registered through the Fabric model loading plugin.
 */
public final class SimpleUnbakedStandaloneModel<T> implements UnbakedExtraModel<T> {
    private final Identifier modelId;
    private final SimpleBaker<T> baker;

    public SimpleUnbakedStandaloneModel(Identifier modelId, SimpleBaker<T> baker) {
        this.modelId = modelId;
        this.baker = baker;
    }

    @Override
    public T bake(ModelBaker modelBaker) {
        return baker.bake(modelBaker.getModel(modelId), modelBaker);
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        resolver.markDependency(modelId);
    }

    public static SimpleUnbakedStandaloneModel<BlockStateModelPart> simpleModelWrapper(Identifier modelId) {
        return simpleModelWrapper(modelId, BlockModelRotation.IDENTITY);
    }

    public static SimpleUnbakedStandaloneModel<BlockStateModelPart> simpleModelWrapper(Identifier modelId,
            ModelState modelState) {
        return new SimpleUnbakedStandaloneModel<>(modelId, (model, baker) -> bakePart(model, baker, modelState));
    }

    public static SimpleUnbakedStandaloneModel<BlockStateModel> blockStateModel(Identifier modelId) {
        return blockStateModel(modelId, BlockModelRotation.IDENTITY);
    }

    public static SimpleUnbakedStandaloneModel<BlockStateModel> blockStateModel(Identifier modelId,
            ModelState modelState) {
        return new SimpleUnbakedStandaloneModel<>(modelId,
                (model, baker) -> new SingleVariant(bakePart(model, baker, modelState)));
    }

    public static SimpleUnbakedStandaloneModel<QuadCollection> quadCollection(Identifier modelId) {
        return quadCollection(modelId, BlockModelRotation.IDENTITY);
    }

    public static SimpleUnbakedStandaloneModel<QuadCollection> quadCollection(Identifier modelId,
            ModelState modelState) {
        return new SimpleUnbakedStandaloneModel<>(modelId,
                (model, baker) -> model.bakeTopGeometry(model.getTopTextureSlots(), baker, modelState));
    }

    private static BlockStateModelPart bakePart(ResolvedModel model, ModelBaker baker, ModelState modelState) {
        var textureSlots = model.getTopTextureSlots();
        return new SimpleModelWrapper(
                model.bakeTopGeometry(textureSlots, baker, modelState),
                model.getTopAmbientOcclusion(),
                model.resolveParticleMaterial(textureSlots, baker));
    }

    @FunctionalInterface
    public interface SimpleBaker<T> {
        T bake(ResolvedModel model, ModelBaker baker);
    }
}
