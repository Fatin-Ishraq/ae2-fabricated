package appeng.platform.neoforge.transfer;

import java.util.Arrays;
import java.util.Objects;
import java.util.SequencedCollection;

import appeng.platform.neoforge.transfer.resource.Resource;
import appeng.platform.neoforge.transfer.transaction.TransactionContext;

/**
 * FABRIC: Concatenates the indices of several handlers.
 */
public class CombinedResourceHandler<T extends Resource> implements ResourceHandler<T> {
    protected final ResourceHandler<T>[] handlers;
    // Index of the first slot of each handler, plus the total size at the end
    protected final int[] baseIndex;

    @SuppressWarnings("unchecked")
    public CombinedResourceHandler(SequencedCollection<? extends ResourceHandler<T>> handlers) {
        this(handlers.toArray(ResourceHandler[]::new));
    }

    @SafeVarargs
    public CombinedResourceHandler(ResourceHandler<T>... handlers) {
        this.handlers = handlers;
        this.baseIndex = new int[handlers.length + 1];
        int index = 0;
        for (int i = 0; i < handlers.length; i++) {
            baseIndex[i] = index;
            index += handlers[i].size();
        }
        baseIndex[handlers.length] = index;
    }

    protected int getHandlerIndex(int index) {
        Objects.checkIndex(index, size());
        int found = Arrays.binarySearch(baseIndex, index);
        if (found >= 0) {
            // Skip handlers of size 0 that share the same base index
            while (found + 1 < handlers.length && baseIndex[found + 1] == index) {
                found++;
            }
            return found;
        }
        return -found - 2;
    }

    protected ResourceHandler<T> getHandlerFromIndex(int handlerIndex) {
        return handlers[handlerIndex];
    }

    protected int getSlotFromIndex(int index, int handlerIndex) {
        return index - baseIndex[handlerIndex];
    }

    @Override
    public final int size() {
        return baseIndex[handlers.length];
    }

    @Override
    public T getResource(int index) {
        int h = getHandlerIndex(index);
        return handlers[h].getResource(getSlotFromIndex(index, h));
    }

    @Override
    public long getAmountAsLong(int index) {
        int h = getHandlerIndex(index);
        return handlers[h].getAmountAsLong(getSlotFromIndex(index, h));
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        int h = getHandlerIndex(index);
        return handlers[h].getCapacityAsLong(getSlotFromIndex(index, h), resource);
    }

    @Override
    public boolean isValid(int index, T resource) {
        int h = getHandlerIndex(index);
        return handlers[h].isValid(getSlotFromIndex(index, h), resource);
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int h = getHandlerIndex(index);
        return handlers[h].insert(getSlotFromIndex(index, h), resource, amount, transaction);
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int inserted = 0;
        for (var handler : handlers) {
            if (inserted >= amount) {
                break;
            }
            inserted += handler.insert(resource, amount - inserted, transaction);
        }
        return inserted;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int h = getHandlerIndex(index);
        return handlers[h].extract(getSlotFromIndex(index, h), resource, amount, transaction);
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int extracted = 0;
        for (var handler : handlers) {
            if (extracted >= amount) {
                break;
            }
            extracted += handler.extract(resource, amount - extracted, transaction);
        }
        return extracted;
    }
}
