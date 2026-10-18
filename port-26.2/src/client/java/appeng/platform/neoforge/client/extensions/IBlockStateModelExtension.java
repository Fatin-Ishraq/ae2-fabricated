package appeng.platform.neoforge.client.extensions;

import java.util.List;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * FABRIC: NeoForge's level-aware {@code collectParts}. Plain models ignore the level; AE2's dynamic models
 * ({@link appeng.platform.neoforge.client.model.DynamicBlockStateModel}) override it. Injected through
 * ae2.classtweaker and {@code appeng.mixins.platform.client.BlockStateModelMixin}.
 */
public interface IBlockStateModelExtension {
    default void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
            List<BlockStateModelPart> parts) {
        ((BlockStateModel) this).collectParts(random, parts);
    }
}
