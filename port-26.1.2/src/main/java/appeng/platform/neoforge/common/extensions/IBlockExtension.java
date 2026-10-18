package appeng.platform.neoforge.common.extensions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * FABRIC: NeoForge's extra methods on blocks that AE2 calls on arbitrary blocks. Injected through ae2.classtweaker
 * and {@code appeng.mixins.platform.BlockMixin}.
 */
public interface IBlockExtension {
    /**
     * Called when the block is set on fire by a player tool. Like NeoForge, this primes vanilla TNT; AE2 blocks
     * override it.
     *
     * @return true if the block reacted to the fire
     */
    default boolean onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction direction,
            @Nullable LivingEntity igniter) {
        if (this instanceof TntBlock) {
            return TntBlock.prime(level, pos);
        }
        return false;
    }
}
