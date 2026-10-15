package com.leclowndu93150.thaumaturge.api.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TransactionTest {
    @Test
    void closingUncommittedScopeRestoresStateWithoutPublishing() {
        Cell cell = new Cell(10);
        try (Transaction scope = Transaction.openRoot()) {
            cell.set(4, scope);
            cell.set(2, scope);
            assertEquals(2, cell.value);
        }
        assertEquals(10, cell.value);
        assertEquals(1, cell.snapshots);
        assertEquals(0, cell.notifications);
    }

    @Test
    void committedRootPublishesOnceWithOriginalState() {
        Cell cell = new Cell(10);
        try (Transaction scope = Transaction.openRoot()) {
            cell.set(4, scope);
            cell.set(2, scope);
            scope.commit();
            assertEquals(0, cell.notifications);
        }
        assertEquals(2, cell.value);
        assertEquals(1, cell.notifications);
        assertEquals(10, cell.original);
    }

    @Test
    void abortedChildRestoresChildEntryStateButKeepsParentChange() {
        Cell cell = new Cell(10);
        try (Transaction root = Transaction.openRoot()) {
            cell.set(8, root);
            try (Transaction child = Transaction.open(root)) {
                cell.set(2, child);
            }
            assertEquals(8, cell.value);
            root.commit();
        }
        assertEquals(8, cell.value);
        assertEquals(1, cell.notifications);
        assertEquals(10, cell.original);
    }

    @Test
    void committedChildStillRollsBackWithOuterScope() {
        Cell parentCell = new Cell(10);
        Cell childCell = new Cell(20);
        try (Transaction root = Transaction.openRoot()) {
            parentCell.set(8, root);
            try (Transaction child = Transaction.open(root)) {
                parentCell.set(2, child);
                childCell.set(3, child);
                child.commit();
            }
            assertEquals(2, parentCell.value);
            assertEquals(3, childCell.value);
        }
        assertEquals(10, parentCell.value);
        assertEquals(20, childCell.value);
        assertEquals(0, parentCell.notifications + childCell.notifications);
    }

    @Test
    void childCommitPreservesEarliestSnapshotAndDefersAllEffects() {
        Cell cell = new Cell(10);
        int[] effects = {0};
        try (Transaction root = Transaction.openRoot()) {
            try (Transaction child = Transaction.open(root)) {
                cell.set(8, child);
                new RootCommitJournal(() -> effects[0]++).updateSnapshots(child);
                child.commit();
            }
            cell.set(5, root);
            assertEquals(0, effects[0]);
            assertEquals(0, cell.notifications);
            root.commit();
        }
        assertEquals(5, cell.value);
        assertEquals(1, cell.notifications);
        assertEquals(10, cell.original);
        assertEquals(1, effects[0]);
    }

    @Test
    void rollbackHappensInReverseParticipationOrder() {
        StringBuilder events = new StringBuilder();
        try (Transaction root = Transaction.openRoot()) {
            journal(events, "first").updateSnapshots(root);
            journal(events, "second").updateSnapshots(root);
        }
        assertEquals("secondfirst", events.toString());
    }

    @Test
    void abortDiscardsCommitOnlyEffects() {
        int[] effects = {0};
        try (Transaction root = Transaction.openRoot()) {
            try (Transaction child = Transaction.open(root)) {
                new RootCommitJournal(() -> effects[0]++).updateSnapshots(child);
                child.commit();
            }
        }
        assertEquals(0, effects[0]);
    }

    @Test
    void exceptionStillRestoresEarlierParticipants() {
        Cell cell = new Cell(10);
        assertThrows(IllegalArgumentException.class, () -> {
            try (Transaction root = Transaction.openRoot()) {
                cell.set(3, root);
                throw new IllegalArgumentException("later provider failed");
            }
        });
        assertEquals(10, cell.value);
        assertEquals(0, cell.notifications);
    }

    @Test
    void independentRootCannotEscapeAnEnclosingTransaction() {
        try (Transaction root = Transaction.openRoot()) {
            assertThrows(IllegalStateException.class, Transaction::openRoot);
            try (Transaction child = Transaction.open(root)) {
                assertThrows(IllegalStateException.class, root::commit);
                assertThrows(IllegalStateException.class, root::close);
            }
        }
    }

    @Test
    void committedOrClosedScopeCannotBeReused() {
        Cell cell = new Cell(10);
        Transaction root = Transaction.openRoot();
        root.commit();
        assertThrows(IllegalStateException.class, () -> cell.set(4, root));
        root.close();
        assertThrows(IllegalStateException.class, () -> Transaction.open(root));
        assertThrows(IllegalStateException.class, root::close);
    }

    private static SnapshotJournal<Integer> journal(StringBuilder events, String name) {
        return new SnapshotJournal<>() {
            @Override
            protected Integer createSnapshot() {
                return 0;
            }

            @Override
            protected void revertToSnapshot(Integer snapshot) {
                events.append(name);
            }
        };
    }

    private static final class Cell extends SnapshotJournal<Integer> {
        private int value;
        private int snapshots;
        private int notifications;
        private int original;

        private Cell(int value) {
            this.value = value;
        }

        private void set(int value, TransactionContext transaction) {
            updateSnapshots(transaction);
            this.value = value;
        }

        @Override
        protected Integer createSnapshot() {
            snapshots++;
            return value;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            value = snapshot;
        }

        @Override
        protected void onRootCommit(Integer snapshot) {
            original = snapshot;
            notifications++;
        }
    }
}
