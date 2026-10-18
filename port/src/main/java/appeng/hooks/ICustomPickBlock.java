package appeng.hooks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public interface ICustomPickBlock {

    /**
     * FABRIC: Same signature as NeoForge's IBlockExtension#getCloneItemStack, invoked through Fabric's
     * ClientPickBlockGatherCallback.
     */
    ItemStack getCloneItemStack(BlockState state,
            HitResult target,
            LevelReader level,
            BlockPos pos,
            Player player);

}
