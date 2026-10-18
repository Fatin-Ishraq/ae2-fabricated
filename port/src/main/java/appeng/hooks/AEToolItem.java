package appeng.hooks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelReader;

/**
 * FABRIC: Items implementing this receive NeoForge-style item hooks that run before the clicked block is activated.
 * They are dispatched by {@link ToolItemHook} from Fabric's UseBlockCallback.
 */
public interface AEToolItem {
    /**
     * Equivalent of NeoForge's IItemExtension#onItemUseFirst. Return anything but PASS to stop further processing.
     */
    default InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return InteractionResult.PASS;
    }

    /**
     * Equivalent of NeoForge's IItemExtension#doesSneakBypassUse: when true, the clicked block is used even while the
     * player is sneaking.
     */
    default boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return false;
    }
}
