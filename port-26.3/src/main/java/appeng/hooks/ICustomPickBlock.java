package appeng.hooks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * FABRIC: Replaces the player-aware {@code getCloneItemStack} of NeoForge's IBlockExtension. Called on the server when
 * a player picks a block (see {@code AppEngBase}, Fabric's PlayerPickItemEvents).
 */
public interface ICustomPickBlock {
    ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData,
            Player player);
}
