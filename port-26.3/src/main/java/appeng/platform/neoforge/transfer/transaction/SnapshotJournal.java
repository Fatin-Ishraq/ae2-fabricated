package appeng.platform.neoforge.transfer.transaction;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext.Result;

/**
 * FABRIC: Journal of snapshots that rolls state back when a transaction is aborted, implemented on top of Fabric's
 * transaction close callbacks (the same model as Fabric's {@code SnapshotParticipant}). Unlike the Fabric class,
 * {@link #onRootCommit} receives the state from before the root transaction, as on NeoForge.
 */
public abstract class SnapshotJournal<T extends @Nullable Object>
        implements net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext.CloseCallback,
        net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext.OuterCloseCallback {
    private static final Object NO_SNAPSHOT = new Object();

    private final ArrayList<Object> snapshots = new ArrayList<>();
    private int snapshotCount;
    private boolean rootCommitScheduled;
    @Nullable
    private T originalState;

    /**
     * @return a copy of the current state, sufficient to revert changes made after this call.
     */
    protected abstract T createSnapshot();

    /**
     * Reverts to a state previously returned by {@link #createSnapshot()}.
     */
    protected abstract void revertToSnapshot(T snapshot);

    /**
     * Called when a snapshot is no longer needed.
     */
    protected void releaseSnapshot(T snapshot) {
    }

    /**
     * Called after the root transaction was committed, with the state from before that transaction.
     */
    protected void onRootCommit(T originalState) {
    }

    public final boolean isInTransaction() {
        return snapshotCount > 0;
    }

    public void updateSnapshots(TransactionContext transaction) {
        updateSnapshots(transaction.fabric());
    }

    public void updateSnapshots(net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
        int depth = transaction.nestingDepth();
        while (snapshots.size() <= depth) {
            snapshots.add(NO_SNAPSHOT);
        }
        if (snapshots.get(depth) == NO_SNAPSHOT) {
            snapshotCount++;
            snapshots.set(depth, createSnapshot());
            transaction.addCloseCallback(this);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onClose(net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction,
            Result result) {
        int depth = transaction.nestingDepth();
        T snapshot = (T) snapshots.set(depth, NO_SNAPSHOT);

        if (result.wasAborted()) {
            revertToSnapshot(snapshot);
            releaseSnapshot(snapshot);
            snapshotCount--;
        } else if (depth == 0) {
            if (!rootCommitScheduled) {
                rootCommitScheduled = true;
                originalState = snapshot;
                transaction.addOuterCloseCallback(this);
            } else {
                // A root commit callback is already pending (the journal was modified from another root commit
                // callback); that callback will see the older original state.
                releaseSnapshot(snapshot);
            }
            snapshotCount = 0;
        } else if (snapshots.get(depth - 1) == NO_SNAPSHOT) {
            // Move the snapshot up to the parent transaction
            snapshots.set(depth - 1, snapshot);
            transaction.getOpenTransaction(depth - 1).addCloseCallback(this);
        } else {
            // The parent already has an older snapshot
            releaseSnapshot(snapshot);
            snapshotCount--;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterOuterClose(Result result) {
        T state = originalState;
        originalState = null;
        rootCommitScheduled = false;
        onRootCommit(state);
        releaseSnapshot(state);
    }
}
