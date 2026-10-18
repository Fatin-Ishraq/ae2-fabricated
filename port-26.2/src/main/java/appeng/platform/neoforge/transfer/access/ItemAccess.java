package appeng.platform.neoforge.transfer.access;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import appeng.platform.neoforge.capabilities.ItemCapability;
import appeng.platform.neoforge.transfer.TransferPreconditions;
import appeng.platform.neoforge.transfer.item.ItemResource;
import appeng.platform.neoforge.transfer.transaction.Transaction;
import appeng.platform.neoforge.transfer.transaction.TransactionContext;

/**
 * FABRIC: Access to the location of an item (a player hand, a slot, a stack), for item capabilities that need to
 * change the item. Backed by a Fabric {@link ContainerItemContext}.
 */
public interface ItemAccess {
    static ItemAccess forPlayerInteraction(Player player, InteractionHand hand) {
        return of(ContainerItemContext.forPlayerInteraction(player, hand));
    }

    static ItemAccess forInfiniteMaterials(Player player, ItemStack contents) {
        return of(ContainerItemContext.forCreativeInteraction(player, contents));
    }

    static ItemAccess forPlayerCursor(Player player, AbstractContainerMenu menu) {
        return of(ContainerItemContext.ofPlayerCursor(player, menu));
    }

    static ItemAccess forPlayerSlot(Player player, int slot) {
        return of(ContainerItemContext.ofPlayerSlot(player, PlayerInventoryStorage.of(player).getSlot(slot)));
    }

    /**
     * An access that reads and writes the given stack directly.
     */
    static ItemAccess forStack(ItemStack stack) {
        var holder = new ItemStack[] { stack };
        return of(ContainerItemContext.ofSingleSlot(new SingleStackStorage() {
            @Override
            protected ItemStack getStack() {
                return holder[0];
            }

            @Override
            protected void setStack(ItemStack newStack) {
                // Keep the identity of the original stack where possible
                if (ItemStack.isSameItemSameComponents(holder[0], newStack) && !holder[0].isEmpty()) {
                    holder[0].setCount(newStack.getCount());
                } else {
                    holder[0] = newStack;
                }
            }
        }));
    }

    /**
     * FABRIC: Wraps a Fabric container item context.
     */
    static ItemAccess of(ContainerItemContext context) {
        return new FabricItemAccess(context);
    }

    /**
     * FABRIC: The equivalent Fabric container item context.
     */
    ContainerItemContext toContext();

    @Nullable
    default <T> T getCapability(ItemCapability<T, ItemAccess> capability) {
        return capability.getCapability(getResource().toStack(), this);
    }

    ItemResource getResource();

    int getAmount();

    int insert(ItemResource resource, int amount, TransactionContext transaction);

    int extract(ItemResource resource, int amount, TransactionContext transaction);

    default int exchange(ItemResource newResource, int amount, @Nullable TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(newResource, amount);
        var currentResource = getResource();
        TransferPreconditions.checkNonEmpty(currentResource);
        try (var tx = Transaction.open(transaction)) {
            int extracted = extract(currentResource, amount, tx);
            if (extracted > 0 && insert(newResource, extracted, tx) == extracted) {
                tx.commit();
                return extracted;
            }
        }
        return 0;
    }

    /**
     * FABRIC: Converts a Fabric container item context into an item access.
     */
    static ItemAccess fromContext(ContainerItemContext context) {
        return of(context);
    }

    record FabricItemAccess(ContainerItemContext context) implements ItemAccess {
        @Override
        public ContainerItemContext toContext() {
            return context;
        }

        @Override
        public ItemResource getResource() {
            return ItemResource.of(context.getItemVariant());
        }

        @Override
        public int getAmount() {
            return context.getItemVariant().isBlank() ? 0 : (int) Math.min(Integer.MAX_VALUE, context.getAmount());
        }

        @Override
        public int insert(ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            return (int) context.insert(resource.toVariant(), amount, transaction.fabric());
        }

        @Override
        public int extract(ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            return (int) context.extract(resource.toVariant(), amount, transaction.fabric());
        }
    }
}
