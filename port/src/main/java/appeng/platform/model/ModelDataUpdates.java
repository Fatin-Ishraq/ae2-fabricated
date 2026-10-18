/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.model;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.blockentity.AEBaseBlockEntity;

/**
 * FABRIC: Replaces NeoForge's BlockEntity#requestModelDataUpdate for code that holds a plain BlockEntity.
 */
public final class ModelDataUpdates {
    private ModelDataUpdates() {
    }

    public static void request(BlockEntity blockEntity) {
        if (blockEntity instanceof AEBaseBlockEntity aeBlockEntity) {
            aeBlockEntity.requestModelDataUpdate();
        } else if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide()) {
            var state = blockEntity.getBlockState();
            blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), state, state, Block.UPDATE_IMMEDIATE);
        }
    }
}
