package appeng.platform.neoforge.common;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;

/**
 * FABRIC: The crafting player context of NeoForge (the player whose crafting consumes the ingredients, used for
 * crafting remainders). Vanilla on Fabric does not read it, but AE2 sets and reads it itself.
 */
public final class CommonHooks {
    private static final ThreadLocal<Player> CRAFTING_PLAYER = new ThreadLocal<>();

    private CommonHooks() {
    }

    public static void setCraftingPlayer(@Nullable Player player) {
        CRAFTING_PLAYER.set(player);
    }

    @Nullable
    public static Player getCraftingPlayer() {
        return CRAFTING_PLAYER.get();
    }
}
