/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.neoforge.registries;

import java.util.function.Function;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/**
 * FABRIC: Minimal stand-in for NeoForge's DeferredBlock.
 */
public class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> implements ItemLike {
    protected DeferredBlock(ResourceKey<Block> key, Function<Identifier, ? extends Block> factory) {
        super(key, factory);
    }

    @Override
    public Item asItem() {
        return get().asItem();
    }
}
