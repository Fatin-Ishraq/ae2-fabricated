/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.model;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;

/**
 * FABRIC: Static versions of the NeoForge BakedModel extension methods that upstream calls on arbitrary models
 * (IBakedModelExtension). Models implementing {@link IDynamicBakedModel} get their dynamic behavior, all other models
 * get the vanilla behavior NeoForge would use by default.
 */
public final class BakedModels {
    private BakedModels() {
    }

    public static List<BakedQuad> getQuads(BakedModel model, @Nullable BlockState state, @Nullable Direction side,
            RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        if (model instanceof IDynamicBakedModel dynamicModel) {
            return dynamicModel.getQuads(state, side, rand, data, renderType);
        }
        return model.getQuads(state, side, rand);
    }

    public static ChunkRenderTypeSet getRenderTypes(BakedModel model, BlockState state, RandomSource rand,
            ModelData data) {
        if (model instanceof IDynamicBakedModel dynamicModel) {
            return dynamicModel.getRenderTypes(state, rand, data);
        }
        return ChunkRenderTypeSet.of(ItemBlockRenderTypes.getChunkRenderType(state));
    }

    public static ModelData getModelData(BakedModel model, net.minecraft.world.level.BlockAndTintGetter level,
            net.minecraft.core.BlockPos pos, BlockState state, ModelData data) {
        if (model instanceof IDynamicBakedModel dynamicModel) {
            return dynamicModel.getModelData(level, pos, state, data);
        }
        return data;
    }

    /**
     * NeoForge's default IBakedModelExtension#applyTransform: applies the model's item transform for the context.
     */
    public static BakedModel applyTransform(BakedModel model, ItemDisplayContext context, PoseStack poseStack,
            boolean applyLeftHandTransform) {
        model.getTransforms().getTransform(context).apply(applyLeftHandTransform, poseStack);
        return model;
    }
}
