/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import appeng.hooks.ICustomBlockDestroyEffect;
import appeng.hooks.ICustomBlockHitEffect;

/**
 * FABRIC: Replicates NeoForge's IClientBlockExtensions#addHitEffects/#addDestroyEffects for blocks implementing the
 * AE2 hook interfaces (the cable bus uses the textures of its parts for particles).
 */
@Mixin(ParticleEngine.class)
public class BlockParticleEffectsMixin {
    @Shadow
    protected ClientLevel level;

    @Inject(method = "destroy", at = @At("HEAD"), cancellable = true)
    private void ae2$customDestroyEffects(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (state.getBlock() instanceof ICustomBlockDestroyEffect effect
                && effect.addDestroyEffects(state, level, pos, (ParticleEngine) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "crack", at = @At("HEAD"), cancellable = true)
    private void ae2$customHitEffects(BlockPos pos, Direction side, CallbackInfo ci) {
        var state = level.getBlockState(pos);
        var hitResult = Minecraft.getInstance().hitResult;
        if (hitResult != null && state.getBlock() instanceof ICustomBlockHitEffect effect
                && effect.addHitEffects(state, level, hitResult, (ParticleEngine) (Object) this)) {
            ci.cancel();
        }
    }
}
