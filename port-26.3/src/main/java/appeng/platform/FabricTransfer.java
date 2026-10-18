package appeng.platform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;

import appeng.platform.neoforge.transfer.ResourceHandler;
import appeng.platform.neoforge.transfer.energy.EnergyHandler;
import appeng.platform.neoforge.transfer.fluid.FluidResource;
import appeng.platform.neoforge.transfer.item.ItemResource;
import appeng.platform.neoforge.transfer.resource.Resource;
import appeng.platform.neoforge.transfer.transaction.TransactionContext;

/**
 * FABRIC: Converts between AE2's NeoForge-style {@link ResourceHandler}/{@link EnergyHandler} and the Fabric transfer
 * API ({@link Storage}) / Team Reborn Energy. Wrapping a wrapper returns the original object, so round trips through
 * Fabric lookups are free.
 */
public final class FabricTransfer {
    private FabricTransfer() {
    }

    // Items

    public static Storage<ItemVariant> toItemStorage(ResourceHandler<ItemResource> handler) {
        return toStorage(handler, ITEMS);
    }

    public static ResourceHandler<ItemResource> fromItemStorage(Storage<ItemVariant> storage) {
        return fromStorage(storage, ITEMS);
    }

    @Nullable
    public static Storage<ItemVariant> toItemStorageOrNull(@Nullable ResourceHandler<ItemResource> handler) {
        return handler == null ? null : toItemStorage(handler);
    }

    @Nullable
    public static ResourceHandler<ItemResource> fromItemStorageOrNull(@Nullable Storage<ItemVariant> storage) {
        return storage == null ? null : fromItemStorage(storage);
    }

    // Fluids

    public static Storage<FluidVariant> toFluidStorage(ResourceHandler<FluidResource> handler) {
        return toStorage(handler, FLUIDS);
    }

    public static ResourceHandler<FluidResource> fromFluidStorage(Storage<FluidVariant> storage) {
        return fromStorage(storage, FLUIDS);
    }

    @Nullable
    public static Storage<FluidVariant> toFluidStorageOrNull(@Nullable ResourceHandler<FluidResource> handler) {
        return handler == null ? null : toFluidStorage(handler);
    }

    @Nullable
    public static ResourceHandler<FluidResource> fromFluidStorageOrNull(@Nullable Storage<FluidVariant> storage) {
        return storage == null ? null : fromFluidStorage(storage);
    }

    // Energy

    public static team.reborn.energy.api.EnergyStorage toEnergyStorage(EnergyHandler handler) {
        if (handler instanceof EnergyFromStorage wrapper) {
            return wrapper.storage;
        }
        return new EnergyToStorage(handler);
    }

    public static EnergyHandler fromEnergyStorage(team.reborn.energy.api.EnergyStorage storage) {
        if (storage instanceof EnergyToStorage wrapper) {
            return wrapper.handler;
        }
        return new EnergyFromStorage(storage);
    }

    @Nullable
    public static team.reborn.energy.api.EnergyStorage toEnergyStorageOrNull(@Nullable EnergyHandler handler) {
        return handler == null ? null : toEnergyStorage(handler);
    }

    @Nullable
    public static EnergyHandler fromEnergyStorageOrNull(@Nullable team.reborn.energy.api.EnergyStorage storage) {
        return storage == null ? null : fromEnergyStorage(storage);
    }

    // Generic implementation

    private record ResourceType<R extends Resource, V extends TransferVariant<?>>(
            Function<R, V> toVariant, Function<V, R> fromVariant, R empty) {
    }

    private static final ResourceType<ItemResource, ItemVariant> ITEMS = new ResourceType<>(
            ItemResource::toVariant, ItemResource::of, ItemResource.EMPTY);

    private static final ResourceType<FluidResource, FluidVariant> FLUIDS = new ResourceType<>(
            FluidResource::toVariant, FluidResource::of, FluidResource.EMPTY);

    @SuppressWarnings("unchecked")
    private static <R extends Resource, V extends TransferVariant<?>> Storage<V> toStorage(
            ResourceHandler<R> handler, ResourceType<R, V> type) {
        if (handler instanceof HandlerFromStorage<?, ?> wrapper && wrapper.type == type) {
            return (Storage<V>) wrapper.storage;
        }
        return new StorageFromHandler<>(handler, type);
    }

    @SuppressWarnings("unchecked")
    private static <R extends Resource, V extends TransferVariant<?>> ResourceHandler<R> fromStorage(
            Storage<V> storage, ResourceType<R, V> type) {
        if (storage instanceof StorageFromHandler<?, ?> wrapper && wrapper.type == type) {
            return (ResourceHandler<R>) wrapper.handler;
        }
        if (storage instanceof SlottedStorage<V> slotted) {
            return new HandlerFromSlottedStorage<>(slotted, type);
        }
        return new HandlerFromStorage<>(storage, type);
    }

    /**
     * Exposes an AE2 resource handler as a Fabric storage with one slot per index.
     */
    private static final class StorageFromHandler<R extends Resource, V extends TransferVariant<?>>
            implements SlottedStorage<V> {
        final ResourceHandler<R> handler;
        final ResourceType<R, V> type;

        StorageFromHandler(ResourceHandler<R> handler, ResourceType<R, V> type) {
            this.handler = handler;
            this.type = type;
        }

        @Override
        public long insert(V resource, long maxAmount,
                net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0) {
                return 0;
            }
            return handler.insert(type.fromVariant.apply(resource), Ints.saturatedCast(maxAmount),
                    TransactionContext.of(transaction));
        }

        @Override
        public long extract(V resource, long maxAmount,
                net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0) {
                return 0;
            }
            return handler.extract(type.fromVariant.apply(resource), Ints.saturatedCast(maxAmount),
                    TransactionContext.of(transaction));
        }

        @Override
        public int getSlotCount() {
            return handler.size();
        }

        @Override
        public SingleSlotStorage<V> getSlot(int slot) {
            return new Slot(slot);
        }

        @Override
        public Iterator<StorageView<V>> iterator() {
            int size = handler.size();
            List<StorageView<V>> views = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                views.add(new Slot(i));
            }
            return views.iterator();
        }

        private final class Slot implements SingleSlotStorage<V> {
            private final int index;

            Slot(int index) {
                this.index = index;
            }

            @Override
            public long insert(V resource, long maxAmount,
                    net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
                if (resource.isBlank() || maxAmount <= 0) {
                    return 0;
                }
                return handler.insert(index, type.fromVariant.apply(resource), Ints.saturatedCast(maxAmount),
                        TransactionContext.of(transaction));
            }

            @Override
            public long extract(V resource, long maxAmount,
                    net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
                if (resource.isBlank() || maxAmount <= 0) {
                    return 0;
                }
                return handler.extract(index, type.fromVariant.apply(resource), Ints.saturatedCast(maxAmount),
                        TransactionContext.of(transaction));
            }

            @Override
            public boolean isResourceBlank() {
                return handler.getResource(index).isEmpty();
            }

            @Override
            public V getResource() {
                return type.toVariant.apply(handler.getResource(index));
            }

            @Override
            public long getAmount() {
                return handler.getResource(index).isEmpty() ? 0 : handler.getAmountAsLong(index);
            }

            @Override
            public long getCapacity() {
                var resource = handler.getResource(index);
                return handler.getCapacityAsLong(index, resource);
            }
        }
    }

    /**
     * Exposes a Fabric storage as an AE2 resource handler. Fabric storages are not necessarily slotted, so the indices
     * are the storage views as seen when {@link #size()} was last called.
     */
    private static class HandlerFromStorage<R extends Resource, V extends TransferVariant<?>>
            implements ResourceHandler<R> {
        final Storage<V> storage;
        final ResourceType<R, V> type;
        private List<StorageView<V>> views = List.of();

        HandlerFromStorage(Storage<V> storage, ResourceType<R, V> type) {
            this.storage = storage;
            this.type = type;
        }

        @Override
        public int size() {
            var list = new ArrayList<StorageView<V>>();
            for (var view : storage) {
                list.add(view);
            }
            views = list;
            return list.size();
        }

        @Nullable
        private StorageView<V> view(int index) {
            if (index >= views.size()) {
                size();
            }
            return index < views.size() ? views.get(index) : null;
        }

        @Override
        public R getResource(int index) {
            var view = view(index);
            return view == null || view.isResourceBlank() ? type.empty : type.fromVariant.apply(view.getResource());
        }

        @Override
        public long getAmountAsLong(int index) {
            var view = view(index);
            return view == null || view.isResourceBlank() ? 0 : view.getAmount();
        }

        @Override
        public long getCapacityAsLong(int index, R resource) {
            var view = view(index);
            return view == null ? 0 : view.getCapacity();
        }

        @Override
        public boolean isValid(int index, R resource) {
            return storage.supportsInsertion();
        }

        @Override
        public int insert(int index, R resource, int amount, TransactionContext transaction) {
            // Views of non-slotted storages cannot be inserted into; insert into the storage instead, but only once
            if (index != 0) {
                return 0;
            }
            return insert(resource, amount, transaction);
        }

        @Override
        public int insert(R resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0) {
                return 0;
            }
            return (int) storage.insert(type.toVariant.apply(resource), amount, transaction.fabric());
        }

        @Override
        public int extract(int index, R resource, int amount, TransactionContext transaction) {
            var view = view(index);
            if (view == null || resource.isEmpty() || amount <= 0) {
                return 0;
            }
            return (int) view.extract(type.toVariant.apply(resource), amount, transaction.fabric());
        }

        @Override
        public int extract(R resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0) {
                return 0;
            }
            return (int) storage.extract(type.toVariant.apply(resource), amount, transaction.fabric());
        }
    }

    /**
     * Exposes a slotted Fabric storage as an AE2 resource handler with one index per slot.
     */
    private static final class HandlerFromSlottedStorage<R extends Resource, V extends TransferVariant<?>>
            extends HandlerFromStorage<R, V> {
        private final SlottedStorage<V> slotted;

        HandlerFromSlottedStorage(SlottedStorage<V> storage, ResourceType<R, V> type) {
            super(storage, type);
            this.slotted = storage;
        }

        @Override
        public int size() {
            return slotted.getSlotCount();
        }

        @Override
        public R getResource(int index) {
            var slot = slotted.getSlot(index);
            return slot.isResourceBlank() ? type.empty : type.fromVariant.apply(slot.getResource());
        }

        @Override
        public long getAmountAsLong(int index) {
            var slot = slotted.getSlot(index);
            return slot.isResourceBlank() ? 0 : slot.getAmount();
        }

        @Override
        public long getCapacityAsLong(int index, R resource) {
            return slotted.getSlot(index).getCapacity();
        }

        @Override
        public int insert(int index, R resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0) {
                return 0;
            }
            return (int) slotted.getSlot(index).insert(type.toVariant.apply(resource), amount,
                    transaction.fabric());
        }

        @Override
        public int extract(int index, R resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0) {
                return 0;
            }
            return (int) slotted.getSlot(index).extract(type.toVariant.apply(resource), amount,
                    transaction.fabric());
        }
    }

    private record EnergyToStorage(EnergyHandler handler) implements team.reborn.energy.api.EnergyStorage {
        @Override
        public long insert(long maxAmount,
                net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
            return maxAmount <= 0 ? 0
                    : handler.insert(Ints.saturatedCast(maxAmount), TransactionContext.of(transaction));
        }

        @Override
        public long extract(long maxAmount,
                net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
            return maxAmount <= 0 ? 0
                    : handler.extract(Ints.saturatedCast(maxAmount), TransactionContext.of(transaction));
        }

        @Override
        public long getAmount() {
            return handler.getAmountAsLong();
        }

        @Override
        public long getCapacity() {
            return handler.getCapacityAsLong();
        }
    }

    private record EnergyFromStorage(team.reborn.energy.api.EnergyStorage storage) implements EnergyHandler {
        @Override
        public long getAmountAsLong() {
            return storage.getAmount();
        }

        @Override
        public long getCapacityAsLong() {
            return storage.getCapacity();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            return amount <= 0 ? 0 : (int) storage.insert(amount, transaction.fabric());
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            return amount <= 0 ? 0 : (int) storage.extract(amount, transaction.fabric());
        }
    }
}
