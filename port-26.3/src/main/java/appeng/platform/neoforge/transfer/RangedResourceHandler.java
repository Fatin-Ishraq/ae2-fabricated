package appeng.platform.neoforge.transfer;

import java.util.Objects;
import java.util.function.Supplier;

import appeng.platform.neoforge.transfer.resource.Resource;
import appeng.platform.neoforge.transfer.transaction.TransactionContext;

/**
 * FABRIC: Exposes the indices {@code [start, end)} of another handler.
 */
public class RangedResourceHandler<T extends Resource> extends DelegatingResourceHandler<T> {
    private final int start;
    private final int end;

    protected RangedResourceHandler(ResourceHandler<T> delegate, int start, int end) {
        this(() -> delegate, start, end);
    }

    protected RangedResourceHandler(Supplier<ResourceHandler<T>> delegate, int start, int end) {
        super(delegate);
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("Invalid range [" + start + ", " + end + ")");
        }
        this.start = start;
        this.end = end;
    }

    public static <T extends Resource> RangedResourceHandler<T> of(ResourceHandler<T> delegate, int start, int end) {
        return new RangedResourceHandler<>(delegate, start, end);
    }

    public static <T extends Resource> RangedResourceHandler<T> of(Supplier<ResourceHandler<T>> delegate, int start,
            int end) {
        return new RangedResourceHandler<>(delegate, start, end);
    }

    public static <T extends Resource> RangedResourceHandler<T> ofSingleIndex(ResourceHandler<T> delegate,
            int index) {
        return of(delegate, index, index + 1);
    }

    public static <T extends Resource> RangedResourceHandler<T> ofSingleIndex(Supplier<ResourceHandler<T>> delegate,
            int index) {
        return of(delegate, index, index + 1);
    }

    @Override
    public int size() {
        return end - start;
    }

    @Override
    protected int convertIndex(int index) {
        Objects.checkIndex(index, size());
        return start + index;
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var handler = getDelegate();
        int inserted = 0;
        for (int i = start; i < end && inserted < amount; i++) {
            inserted += handler.insert(i, resource, amount - inserted, transaction);
        }
        return inserted;
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var handler = getDelegate();
        int extracted = 0;
        for (int i = start; i < end && extracted < amount; i++) {
            extracted += handler.extract(i, resource, amount - extracted, transaction);
        }
        return extracted;
    }
}
