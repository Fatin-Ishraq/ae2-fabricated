/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.model;

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * FABRIC: Equivalent of NeoForge's IDynamicBakedModel. Upstream AE2 builds its dynamic models as lists of baked quads
 * that depend on {@link ModelData} and the requested {@link RenderType}. This interface keeps that model code
 * unchanged and bridges it to the Fabric Renderer API:
 * <ul>
 * <li>Block rendering goes through {@link #emitBlockQuads}, which fetches the block entity's model data via
 * {@code getBlockEntityRenderData}, lets the model augment it ({@link #getModelData}), and emits the quads for each
 * render type returned by {@link #getRenderTypes} with a matching blend mode.</li>
 * <li>Item rendering uses Fabric's default vanilla path, which calls {@link #getQuads(BlockState, Direction,
 * RandomSource)} with {@link ModelData#EMPTY}.</li>
 * </ul>
 */
public interface IDynamicBakedModel extends BakedModel {

    List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
            ModelData extraData, @Nullable RenderType renderType);

    @Override
    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    /**
     * Allows the model to add data computed from the world (NeoForge: IBakedModelExtension#getModelData).
     */
    default ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        return modelData;
    }

    /**
     * The render types this model emits quads for (NeoForge: IBakedModelExtension#getRenderTypes).
     */
    default ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return ChunkRenderTypeSet.of(ItemBlockRenderTypes.getChunkRenderType(state));
    }

    /**
     * NeoForge provides a default for this (no transforms); vanilla's BakedModel does not.
     */
    @Override
    default ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    default TextureAtlasSprite getParticleIcon(ModelData data) {
        return getParticleIcon();
    }

    @Override
    default boolean isVanillaAdapter() {
        return false;
    }

    @Override
    default void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos,
            Supplier<RandomSource> randomSupplier, RenderContext context) {
        var data = getModelData(blockView, pos, state, ModelData.of(blockView.getBlockEntityRenderData(pos)));
        emitQuads(this, state, data, randomSupplier, context);
    }

    /**
     * Emits the quads of a model built for NeoForge's quad-list pipeline into a Fabric render context.
     */
    static void emitQuads(IDynamicBakedModel model, BlockState state, ModelData data,
            Supplier<RandomSource> randomSupplier, RenderContext context) {
        var renderTypes = model.getRenderTypes(state, randomSupplier.get(), data);
        var emitter = context.getEmitter();
        var ao = model.useAmbientOcclusion() ? TriState.DEFAULT : TriState.FALSE;

        for (var renderType : renderTypes) {
            var material = getMaterial(renderType, ao);
            for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
                var cullFace = ModelHelper.faceFromIndex(i);
                var quads = model.getQuads(state, cullFace, randomSupplier.get(), data, renderType);
                for (var quad : quads) {
                    emitter.fromVanilla(quad, material, cullFace);
                    emitter.emit();
                }
            }
        }
    }

    private static RenderMaterial getMaterial(RenderType renderType, TriState ao) {
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        return renderer.materialFinder()
                .blendMode(BlendMode.fromRenderLayer(renderType))
                .ambientOcclusion(ao)
                .find();
    }
}
