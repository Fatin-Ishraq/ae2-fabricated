package appeng.platform.neoforge.transfer;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import appeng.platform.neoforge.transfer.resource.Resource;
import appeng.platform.neoforge.transfer.resource.ResourceStack;
import appeng.platform.neoforge.transfer.transaction.TransactionContext;

/**
 * FABRIC: Helpers for resource handlers (the subset AE2 uses).
 */
public final class ResourceHandlerUtil {
    private ResourceHandlerUtil() {
    }

    /**
     * Inserts into indices that already contain the resource first, then into the remaining ones.
     */
    public static <T extends Resource> int insertStacking(ResourceHandler<T> handler, T resource, int amount,
            TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int inserted = 0;
        int size = handler.size();
        for (int i = 0; i < size && inserted < amount; i++) {
            if (resource.equals(handler.getResource(i))) {
                inserted += handler.insert(i, resource, amount - inserted, transaction);
            }
        }
        for (int i = 0; i < size && inserted < amount; i++) {
            if (handler.getResource(i).isEmpty()) {
                inserted += handler.insert(i, resource, amount - inserted, transaction);
            }
        }
        return inserted;
    }

    /**
     * Extracts up to {@code maxAmount} of the first resource accepted by the filter.
     *
     * @return what was extracted, or null if nothing was
     */
    @Nullable
    public static <T extends Resource> ResourceStack<T> extractFirst(ResourceHandler<T> handler, Predicate<T> filter,
            int maxAmount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(maxAmount);
        for (int i = 0, size = handler.size(); i < size; i++) {
            var resource = handler.getResource(i);
            if (resource.isEmpty() || !filter.test(resource)) {
                continue;
            }
            int extracted = handler.extract(resource, maxAmount, transaction);
            if (extracted > 0) {
                return new ResourceStack<>(resource, extracted);
            }
        }
        return null;
    }
}
