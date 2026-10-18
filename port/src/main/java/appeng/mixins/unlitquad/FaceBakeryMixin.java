/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.mixins.unlitquad;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;

import appeng.hooks.UnlitQuadHooks;

/**
 * FABRIC: Applies NeoForge-style per-face light emission when a face is baked, see {@link UnlitQuadHooks}.
 */
@Mixin(FaceBakery.class)
public class FaceBakeryMixin {
    @Inject(method = "bakeQuad", at = @At("RETURN"), cancellable = true)
    private void ae2$applyLightEmission(Vector3f posFrom, Vector3f posTo, BlockElementFace face,
            TextureAtlasSprite sprite, Direction facing, ModelState transform, BlockElementRotation rotation,
            boolean shade, CallbackInfoReturnable<BakedQuad> cir) {
        var quad = UnlitQuadHooks.onFaceBaked(face, cir.getReturnValue());
        if (quad != cir.getReturnValue()) {
            cir.setReturnValue(quad);
        }
    }
}
