package com.leclowndu93150.thaumaturge.api.transaction;

import java.util.Objects;

/** Defers an effect until the root transaction commits. Aborting discards the effect. */
public final class RootCommitJournal extends SnapshotJournal<Boolean> {
    private final Runnable action;

    public RootCommitJournal(Runnable action) {
        this.action = Objects.requireNonNull(action, "action");
    }

    @Override
    protected Boolean createSnapshot() {
        return Boolean.TRUE;
    }

    @Override
    protected void revertToSnapshot(Boolean snapshot) {}

    @Override
    protected void onRootCommit(Boolean original) {
        action.run();
    }
}
