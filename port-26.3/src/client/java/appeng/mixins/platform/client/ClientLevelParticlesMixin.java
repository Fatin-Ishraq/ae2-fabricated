package appeng.mixins.platform.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import appeng.platform.neoforge.client.extensions.common.IClientBlockExtensions;

/**
 * FABRIC: Lets {@link IClientBlockExtensions} replace the block hit and destroy particles, like NeoForge does.
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelParticlesMixin {
    @Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
    private void ae2$customDestroyEffects(BlockPos pos, BlockState state, CallbackInfo ci) {
        var level = (ClientLevel) (Object) this;
        var extensions = IClientBlockExtensions.of(state);
        if (extensions != IClientBlockExtensions.DEFAULT
                && extensions.addDestroyEffects(state, level, pos, Minecraft.getInstance().particleEngine)) {
            ci.cancel();
        }
    }

    // 26.3 split the hit effects into particles and sound; only the particles are replaced, like NeoForge
    @Inject(method = "addBreakingParticles", at = @At("HEAD"), cancellable = true)
    private void ae2$customHitEffects(BlockPos pos, Direction side, BlockState state, CallbackInfo ci) {
        var level = (ClientLevel) (Object) this;
        var extensions = IClientBlockExtensions.of(state);
        if (extensions != IClientBlockExtensions.DEFAULT
                && extensions.addHitEffects(state, level, pos, side, Minecraft.getInstance().particleEngine)) {
            ci.cancel();
        }
    }
}
