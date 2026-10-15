package com.leclowndu93150.thaumaturge.api.transaction;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * A synchronous rollback scope. Closing without {@link #commit()} restores participants in
 * reverse order. A committed child transfers rollback responsibility to its parent; only a
 * committed root publishes effects. Scopes must be opened and closed in nesting order on one
 * thread. A participant that throws while restoring violates the transaction contract.
 */
public final class Transaction implements TransactionContext, AutoCloseable {
    private static final ThreadLocal<Transaction> CURRENT = new ThreadLocal<>();

    private final @Nullable Transaction parent;
    private final Thread owner = Thread.currentThread();
    private final Map<SnapshotJournal<?>, Entry<?>> snapshots = new IdentityHashMap<>();
    private final List<Entry<?>> ordered = new ArrayList<>();
    private boolean committed;
    private boolean closed;

    private Transaction(@Nullable Transaction parent) {
        this.parent = parent;
        CURRENT.set(this);
    }

    /** Opens a root scope; no other scope may be open on this thread. */
    public static Transaction openRoot() {
        return open(null);
    }

    /** Opens a child of the current scope, or a root when {@code parent} is null. */
    public static Transaction open(@Nullable TransactionContext parent) {
        Transaction enclosing = (Transaction) parent;
        if (enclosing != null) {
            enclosing.checkActive();
        } else if (CURRENT.get() != null) {
            throw new IllegalStateException("An open transaction must be supplied as the parent");
        }
        return new Transaction(enclosing);
    }

    /** Marks this scope successful. The scope must still be closed. */
    public void commit() {
        checkActive();
        committed = true;
    }

    <S> void enlist(SnapshotJournal<S> journal) {
        checkActive();
        if (!snapshots.containsKey(journal)) {
            Entry<S> entry = new Entry<>(journal, journal.createSnapshot());
            snapshots.put(journal, entry);
            ordered.add(entry);
        }
    }

    private void checkActive() {
        if (Thread.currentThread() != owner || CURRENT.get() != this || closed || committed) {
            throw new IllegalStateException("Transaction is not the active uncommitted scope");
        }
    }

    @Override
    public void close() {
        if (closed || Thread.currentThread() != owner || CURRENT.get() != this) {
            throw new IllegalStateException("Transactions must close once, in nesting order on their owning thread");
        }
        closed = true;
        if (parent == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(parent);
        }
        if (committed && parent != null) {
            for (Entry<?> entry : ordered) {
                if (!parent.snapshots.containsKey(entry.journal())) {
                    parent.snapshots.put(entry.journal(), entry);
                    parent.ordered.add(entry);
                }
            }
            return;
        }
        RuntimeException failure = null;
        for (int index = 0; index < ordered.size(); index++) {
            Entry<?> entry = ordered.get(committed ? index : ordered.size() - 1 - index);
            try {
                if (committed) {
                    entry.publish();
                } else {
                    entry.restore();
                }
            } catch (RuntimeException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    private record Entry<S>(SnapshotJournal<S> journal, S snapshot) {
        void restore() {
            journal.revertToSnapshot(snapshot);
        }

        void publish() {
            journal.onRootCommit(snapshot);
        }
    }
}
