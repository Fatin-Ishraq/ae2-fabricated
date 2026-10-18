/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.registries;

import java.util.function.Function;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * FABRIC: Minimal stand-in for NeoForge's DeferredItem.
 */
public class DeferredItem<T extends Item> extends DeferredHolder<Item, T> implements ItemLike {
    protected DeferredItem(ResourceKey<Item> key, Function<ResourceLocation, ? extends Item> factory) {
        super(key, factory);
    }

    @Override
    public Item asItem() {
        return get();
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public ItemStack toStack(int count) {
        return new ItemStack(get(), count);
    }
}
