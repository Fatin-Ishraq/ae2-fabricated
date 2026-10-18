package appeng.platform.neoforge.transfer;

import appeng.platform.neoforge.transfer.resource.Resource;

/**
 * FABRIC: Directly sets the contents of an index, bypassing validation and transactions.
 */
@FunctionalInterface
public interface IndexModifier<T extends Resource> {
    void set(int index, T resource, int amount);
}
