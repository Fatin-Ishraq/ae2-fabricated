package appeng.platform.neoforge.transfer;

import java.util.Objects;
import java.util.function.Supplier;

import appeng.platform.neoforge.transfer.resource.Resource;
import appeng.platform.neoforge.transfer.transaction.TransactionContext;

/**
 * FABRIC: Forwards all operations to another handler, with an overridable index mapping.
 */
public class DelegatingResourceHandler<T extends Resource> implements ResourceHandler<T> {
    protected final Supplier<ResourceHandler<T>> delegate;

    public DelegatingResourceHandler(ResourceHandler<T> delegate) {
        Objects.requireNonNull(delegate);
        this.delegate = () -> delegate;
    }

    public DelegatingResourceHandler(Supplier<ResourceHandler<T>> delegate) {
        this.delegate = delegate;
    }

    public ResourceHandler<T> getDelegate() {
        return delegate.get();
    }

    protected int convertIndex(int index) {
        Objects.checkIndex(index, size());
        return index;
    }

    @Override
    public int size() {
        return getDelegate().size();
    }

    @Override
    public T getResource(int index) {
        return getDelegate().getResource(convertIndex(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return getDelegate().getAmountAsLong(convertIndex(index));
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        return getDelegate().getCapacityAsLong(convertIndex(index), resource);
    }

    @Override
    public boolean isValid(int index, T resource) {
        return getDelegate().isValid(convertIndex(index), resource);
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return getDelegate().insert(convertIndex(index), resource, amount, transaction);
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        return getDelegate().insert(resource, amount, transaction);
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return getDelegate().extract(convertIndex(index), resource, amount, transaction);
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        return getDelegate().extract(resource, amount, transaction);
    }
}
