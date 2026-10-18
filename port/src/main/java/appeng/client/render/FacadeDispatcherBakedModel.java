/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;

import appeng.client.render.cablebus.FacadeBuilder;
import appeng.items.parts.FacadeItem;

/**
 * This baked model class is used as a dispatcher to redirect the renderer to the *real* model that should be used based
 * on the item stack. A custom Item Override List is used to accomplish this.
 */
public class FacadeDispatcherBakedModel extends DelegateBakedModel {
    private final FacadeBuilder facadeBuilder;
    // FABRIC: vanilla equivalent of NeoForge's ItemStackMap.createTypeAndTagMap()
    private final Map<ItemStack, FacadeBakedItemModel> cache = new Object2ObjectOpenCustomHashMap<>(
            ItemStackLinkedSet.TYPE_AND_TAG);

    public FacadeDispatcherBakedModel(BakedModel baseModel, FacadeBuilder facadeBuilder) {
        super(baseModel);
        this.facadeBuilder = facadeBuilder;
    }

    public synchronized List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        if (!(stack.getItem() instanceof FacadeItem itemFacade)) {
            return List.of(this);
        }

        var textureItem = itemFacade.getTextureItem(stack);
        var model = cache.get(textureItem);
        if (model == null) {
            model = new FacadeBakedItemModel(getBaseModel(), textureItem, facadeBuilder);
            cache.put(textureItem, model);
        }

        return List.of(model);
    }

    /**
     * FABRIC: NeoForge renders the models returned by {@link #getRenderPasses}. On Fabric, item quads are emitted
     * through the Fabric Renderer API instead.
     */
    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitItemQuads(ItemStack stack, java.util.function.Supplier<net.minecraft.util.RandomSource> randomSupplier,
            net.fabricmc.fabric.api.renderer.v1.render.RenderContext context) {
        for (var model : getRenderPasses(stack, false)) {
            if (model == this) {
                super.emitItemQuads(stack, randomSupplier, context);
            } else {
                model.emitItemQuads(stack, randomSupplier, context);
            }
        }
    }
}
