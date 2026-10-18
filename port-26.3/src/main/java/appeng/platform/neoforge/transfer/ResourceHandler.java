package appeng.platform.neoforge.transfer;

import com.google.common.primitives.Ints;

import appeng.platform.neoforge.transfer.resource.Resource;
import appeng.platform.neoforge.transfer.transaction.TransactionContext;

/**
 * FABRIC: An index-based container of resources (the NeoForge 26.x transfer model), used inside AE2. At the platform
 * boundary it is converted to and from Fabric {@code Storage} by {@link appeng.platform.FabricTransfer}.
 */
public interface ResourceHandler<T extends Resource> {
    int size();

    T getResource(int index);

    long getAmountAsLong(int index);

    default int getAmountAsInt(int index) {
        return Ints.saturatedCast(getAmountAsLong(index));
    }

    long getCapacityAsLong(int index, T resource);

    default int getCapacityAsInt(int index, T resource) {
        return Ints.saturatedCast(getCapacityAsLong(index, resource));
    }

    boolean isValid(int index, T resource);

    int insert(int index, T resource, int amount, TransactionContext transaction);

    default int insert(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int inserted = 0;
        for (int i = 0, size = size(); i < size && inserted < amount; i++) {
            inserted += insert(i, resource, amount - inserted, transaction);
        }
        return inserted;
    }

    int extract(int index, T resource, int amount, TransactionContext transaction);

    default int extract(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int extracted = 0;
        for (int i = 0, size = size(); i < size && extracted < amount; i++) {
            extracted += extract(i, resource, amount - extracted, transaction);
        }
        return extracted;
    }

    @SuppressWarnings("unchecked")
    static <T extends Resource> Class<ResourceHandler<T>> asClass() {
        return (Class<ResourceHandler<T>>) (Object) ResourceHandler.class;
    }
}
