package appeng.platform.neoforge.capabilities;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * FABRIC: Provides a capability for a block.
 */
@FunctionalInterface
public interface IBlockCapabilityProvider<T, C> {
    @Nullable
    T getCapability(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, C context);
}
