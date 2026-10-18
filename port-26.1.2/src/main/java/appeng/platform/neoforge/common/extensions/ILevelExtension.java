package appeng.platform.neoforge.common.extensions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import appeng.platform.neoforge.capabilities.BlockCapability;
import appeng.platform.neoforge.capabilities.ICapabilityInvalidationListener;

/**
 * FABRIC: NeoForge's capability methods on {@link Level}. Injected through ae2.classtweaker and
 * {@code appeng.mixins.platform.LevelMixin}.
 */
public interface ILevelExtension {
    @Nullable
    default <T, C> T getCapability(BlockCapability<T, C> capability, BlockPos pos, C context) {
        return capability.getCapability((Level) this, pos, null, null, context);
    }

    @Nullable
    default <T, C> T getCapability(BlockCapability<T, C> capability, BlockPos pos, @Nullable BlockState state,
            @Nullable BlockEntity blockEntity, C context) {
        return capability.getCapability((Level) this, pos, state, blockEntity, context);
    }

    /**
     * Notifies capability listeners for the position (Fabric lookups themselves are not cached).
     */
    default void invalidateCapabilities(BlockPos pos) {
        appeng.platform.neoforge.capabilities.CapabilityListeners.invalidate((Level) this, pos);
    }

    /**
     * See {@link appeng.platform.neoforge.capabilities.CapabilityListeners}.
     */
    default void registerCapabilityListener(BlockPos pos, ICapabilityInvalidationListener listener) {
        if (this instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            appeng.platform.neoforge.capabilities.CapabilityListeners.register(serverLevel, pos, listener);
        }
    }
}
