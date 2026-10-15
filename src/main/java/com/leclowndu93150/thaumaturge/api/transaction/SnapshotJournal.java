package com.leclowndu93150.thaumaturge.api.transaction;

/**
 * Journals an owned resource once per transaction scope. Keep one journal per resource, rather
 * than allocating a journal for every mutation, so nested scopes retain the earliest snapshot.
 *
 * @param <S> an independent snapshot of the resource's mutable state
 */
public abstract class SnapshotJournal<S> {
    /** Records the current state before the first mutation in this scope. */
    public final void updateSnapshots(TransactionContext transaction) {
        ((Transaction) transaction).enlist(this);
    }

    protected abstract S createSnapshot();

    /** Restores state without notifications or further transactional mutations. */
    protected abstract void revertToSnapshot(S snapshot);

    /** Runs once after the outermost commit; never runs for an aborted scope. */
    protected void onRootCommit(S original) {}
}
