package appeng.platform.neoforge.transfer;

import appeng.platform.neoforge.transfer.resource.Resource;
import appeng.platform.neoforge.transfer.transaction.TransactionContext;

/**
 * FABRIC: A handler without any indices.
 */
public final class EmptyResourceHandler<T extends Resource> implements ResourceHandler<T> {
    private static final EmptyResourceHandler<?> INSTANCE = new EmptyResourceHandler<>();

    private EmptyResourceHandler() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> EmptyResourceHandler<T> instance() {
        return (EmptyResourceHandler<T>) INSTANCE;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public T getResource(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public long getAmountAsLong(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public boolean isValid(int index, T resource) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return 0;
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return 0;
    }

    @Override
    public String toString() {
        return "EmptyResourceHandler";
    }
}
