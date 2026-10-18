/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.mixins.unlitquad;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.block.model.BlockElementFace;

import appeng.hooks.UnlitQuadHooks;

/**
 * FABRIC: Reads NeoForge's per-face "neoforge_data" light emission, see {@link UnlitQuadHooks}.
 */
@Mixin(targets = "net.minecraft.client.renderer.block.model.BlockElementFace$Deserializer")
public class BlockElementFaceDeserializerMixin {
    @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockElementFace;", at = @At("RETURN"))
    private void ae2$readLightEmission(JsonElement json, Type type, JsonDeserializationContext context,
            CallbackInfoReturnable<BlockElementFace> cir) {
        UnlitQuadHooks.onFaceDeserialized(cir.getReturnValue(), json);
    }
}
