package appeng.platform.neoforge.transfer.item;

import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.minecraft.world.inventory.AbstractContainerMenu;

import appeng.platform.FabricTransfer;
import appeng.platform.neoforge.transfer.ResourceHandler;

/**
 * FABRIC: The stack carried by the cursor in a menu as a resource handler, backed by Fabric's cursor storage.
 */
public final class CarriedSlotWrapper {
    private CarriedSlotWrapper() {
    }

    public static ResourceHandler<ItemResource> of(AbstractContainerMenu menu) {
        return FabricTransfer.fromItemStorage(PlayerInventoryStorage.getCursorStorage(menu));
    }
}
