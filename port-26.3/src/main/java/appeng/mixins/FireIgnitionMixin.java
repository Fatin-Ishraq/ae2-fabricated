/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.mixins;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

import appeng.block.misc.TinyTNTBlock;

/**
 * FABRIC: NeoForge calls IBlockExtension#onCaughtFire for any block burned by fire; vanilla only ignites TNT. This
 * lets fire ignite AE2's Tiny TNT.
 */
@Mixin(FireBlock.class)
public class FireIgnitionMixin {
    @Inject(method = "checkBurnOut", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"))
    private void ae2$igniteTinyTnt(Level level, BlockPos pos, int chance, RandomSource random, int age,
            CallbackInfo ci, @Local BlockState blockState) {
        if (blockState.getBlock() instanceof TinyTNTBlock tinyTnt) {
            tinyTnt.onCaughtFire(blockState, level, pos, null, null, net.minecraft.world.item.ItemStack.EMPTY);
        }
    }
}
