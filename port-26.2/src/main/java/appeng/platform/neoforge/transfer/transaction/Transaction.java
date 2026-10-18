package appeng.platform.neoforge.transfer.transaction;

import org.jetbrains.annotations.Nullable;

/**
 * FABRIC: NeoForge-style transaction handle on top of a Fabric transfer API transaction.
 */
public final class Transaction implements AutoCloseable, TransactionContext {
    private final net.fabricmc.fabric.api.transfer.v1.transaction.Transaction fabric;

    Transaction(net.fabricmc.fabric.api.transfer.v1.transaction.Transaction fabric) {
        this.fabric = fabric;
    }

    /**
     * Opens a new root transaction. Fails if a transaction is already open on this thread.
     */
    public static Transaction openRoot() {
        return new Transaction(net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter());
    }

    /**
     * Opens a nested transaction if a parent is given, or a root transaction otherwise.
     */
    public static Transaction open(@Nullable TransactionContext parent) {
        if (parent == null) {
            return openRoot();
        }
        return new Transaction(parent.fabric().openNested());
    }

    /**
     * @return the currently open transaction on this thread, or null if there is none.
     */
    @Nullable
    @Deprecated
    public static TransactionContext getCurrentOpenedTransaction() {
        if (!net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.isOpen()) {
            return null;
        }
        return TransactionContext
                .of(net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.getCurrentUnsafe());
    }

    public void commit() {
        fabric.commit();
    }

    @Override
    public void close() {
        fabric.close();
    }

    @Override
    public int depth() {
        return fabric.nestingDepth();
    }

    @Override
    public net.fabricmc.fabric.api.transfer.v1.transaction.Transaction fabric() {
        return fabric;
    }

    @Override
    public String toString() {
        return "Transaction[depth=" + depth() + "]";
    }
}
