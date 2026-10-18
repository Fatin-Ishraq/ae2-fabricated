package appeng.mixins.platform;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import appeng.platform.neoforge.capabilities.CapabilityListeners;

/**
 * FABRIC: Notifies capability listeners when the block at a position changes, like NeoForge does.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelCapabilityMixin {
    @Inject(method = "updatePOIOnBlockStateChange", at = @At("HEAD"))
    private void ae2$invalidateCapabilities(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        if (oldState.getBlock() != newState.getBlock()) {
            CapabilityListeners.invalidate((ServerLevel) (Object) this, pos);
        }
    }
}
