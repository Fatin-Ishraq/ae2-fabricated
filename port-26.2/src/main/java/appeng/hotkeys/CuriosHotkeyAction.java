package appeng.hotkeys;

import java.util.function.Predicate;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import appeng.api.features.HotkeyAction;
import appeng.menu.locator.MenuLocators;

public record CuriosHotkeyAction(Predicate<ItemStack> locatable,
        InventoryHotkeyAction.Opener opener) implements HotkeyAction {

    public CuriosHotkeyAction(ItemLike item, InventoryHotkeyAction.Opener opener) {
        this((stack) -> stack.is(item.asItem()), opener);
    }

    @Override
    public boolean run(Player player) {
        // FABRIC: Curios is NeoForge-only; there is no accessory inventory to search yet.
        return false;
    }
}
