package appeng.platform.neoforge.transfer.item;

import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import appeng.platform.FabricTransfer;
import appeng.platform.neoforge.transfer.ResourceHandler;

/**
 * FABRIC: The player inventory as a resource handler, backed by Fabric's {@link PlayerInventoryStorage}.
 */
public final class PlayerInventoryWrapper {
    private PlayerInventoryWrapper() {
    }

    public static ResourceHandler<ItemResource> of(Player player) {
        return of(player.getInventory());
    }

    public static ResourceHandler<ItemResource> of(Inventory inventory) {
        return FabricTransfer.fromItemStorage(PlayerInventoryStorage.of(inventory));
    }
}
