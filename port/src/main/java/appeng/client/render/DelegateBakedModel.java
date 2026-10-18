/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.render;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import appeng.platform.model.ChunkRenderTypeSet;
import appeng.platform.model.IDynamicBakedModel;
import appeng.platform.model.BakedModels;
import appeng.platform.model.ModelData;

public abstract class DelegateBakedModel implements IDynamicBakedModel {
    private final BakedModel baseModel;

    protected DelegateBakedModel(BakedModel base) {
        this.baseModel = base;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource rand) {
        return baseModel.getQuads(state, direction, rand);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
            @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        return BakedModels.getQuads(baseModel, state, side, rand, data, renderType);
    }

    @Override
    public boolean usesBlockLight() {
        return baseModel.usesBlockLight();
    }

    @Override
    public ItemOverrides getOverrides() {
        return baseModel.getOverrides();
    }

    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack,
            boolean applyLeftHandTransform) {
        BakedModels.applyTransform(baseModel, transformType, poseStack, applyLeftHandTransform);
        return this;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.baseModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.baseModel.getTransforms();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return BakedModels.getRenderTypes(baseModel, state, rand, data);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.baseModel.isGui3d();
    }

    @Override
    public boolean isCustomRenderer() {
        return this.baseModel.isCustomRenderer();
    }

    public BakedModel getBaseModel() {
        return this.baseModel;
    }
}
