package appeng.platform.neoforge.transfer.transaction;

/**
 * FABRIC: A transaction context backed by a Fabric transfer API transaction, so that AE2 and other Fabric mods take
 * part in the same transactions.
 */
public interface TransactionContext {
    /**
     * @return the nesting depth of this transaction; 0 for the root transaction.
     */
    int depth();

    /**
     * @return the underlying Fabric transaction context.
     */
    net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext fabric();

    /**
     * Wraps a Fabric transaction context, for example one passed to a Fabric storage adapter.
     */
    static TransactionContext of(net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext fabric) {
        if (fabric instanceof net.fabricmc.fabric.api.transfer.v1.transaction.Transaction tx) {
            return new Transaction(tx);
        }
        return new Wrapped(fabric);
    }

    record Wrapped(net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext fabric)
            implements TransactionContext {
        @Override
        public int depth() {
            return fabric.nestingDepth();
        }
    }
}
