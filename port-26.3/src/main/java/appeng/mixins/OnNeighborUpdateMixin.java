package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import appeng.hooks.INeighborChangeSensitive;

/**
 * FABRIC: Replicates NeoForge's IBlockExtension#onNeighborChange callback, which NeoForge fires from
 * Level#updateNeighbourForOutputSignal for the horizontal neighbors of a block whose comparator output changed.
 */
@Mixin(Level.class)
public abstract class OnNeighborUpdateMixin {

    @Inject(method = "updateNeighbourForOutputSignal", at = @At("HEAD"))
    private void ae2$triggerOnNeighborChange(BlockPos srcPos, Block srcBlock, CallbackInfo ci) {
        Level level = (Level) (Object) this;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            var blockPos = srcPos.relative(direction);
            if (level.hasChunkAt(blockPos)) {
                var blockState = level.getBlockState(blockPos);
                if (blockState.getBlock() instanceof INeighborChangeSensitive sensitive) {
                    sensitive.onNeighborChange(blockState, level, blockPos, srcPos);
                }
            }
        }
    }

}
