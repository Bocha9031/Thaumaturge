package com.leclowndu93150.thaumaturge.content.essentia.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import com.leclowndu93150.thaumaturge.api.transaction.Transaction;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.junit.jupiter.api.Test;

class SingleAspectStorageTransactionTest {
    private static final Holder<IAspect> AER = aspect(TTAspects.AER);

    @Test
    void abortRestoresContentsAndDoesNotPublishRevisionOrSync() {
        TestHost host = new TestHost(64, 64);
        IEssentiaStorage storage = new SingleAspectStorage(host).view(Direction.UP);

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(8, storage.insert(AER, 8, transaction));
            assertEquals(8, host.amount);
            assertEquals(0, storage.contentRevision());
        }

        assertEquals(0, host.amount);
        assertNull(host.aspect);
        assertEquals(0, storage.contentRevision());
        assertEquals(0, host.syncs);
    }

    @Test
    void simulateConvenienceDoesNotMutateWhileImmediateInsertCommits() {
        TestHost host = new TestHost(64, 64);
        IEssentiaStorage storage = new SingleAspectStorage(host).view(Direction.UP);

        assertEquals(8, storage.insert(AER, 8, true));
        assertEquals(0, host.amount);
        assertEquals(0, storage.contentRevision());
        assertEquals(0, host.syncs);

        assertEquals(8, storage.insert(AER, 8, false));
        assertEquals(8, host.amount);
        assertEquals(1, storage.contentRevision());
        assertEquals(1, host.syncs);
    }

    @Test
    void nestedCommitPublishesOnceOnlyWhenRootCommits() {
        TestHost host = new TestHost(64, 64);
        IEssentiaStorage storage = new SingleAspectStorage(host).view(Direction.UP);

        try (Transaction root = Transaction.openRoot()) {
            try (Transaction child = Transaction.open(root)) {
                assertEquals(5, storage.insert(AER, 5, child));
                child.commit();
            }
            assertEquals(0, storage.contentRevision());
            root.commit();
        }

        assertEquals(5, host.amount);
        assertEquals(1, storage.contentRevision());
        assertEquals(1, host.syncs);
    }

    @Test
    void overflowEffectsAreDeferredAndRolledBackWithTheContents() {
        TestHost host = new TestHost(2, 64);
        IEssentiaStorage storage = new SingleAspectStorage(host).view(Direction.UP);

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(5, storage.insert(AER, 5, transaction));
        }
        assertEquals(0, host.amount);
        assertEquals(0, host.voided);

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(5, storage.insert(AER, 5, transaction));
            transaction.commit();
        }
        assertEquals(2, host.amount);
        assertEquals(3, host.voided);
        assertEquals(1, storage.contentRevision());
    }

    @Test
    void extractionParticipatesInCallerRollback() {
        TestHost host = new TestHost(64, 64);
        host.aspect = TTAspects.AER;
        host.amount = 9;
        assertEquals(TTAspects.AER, AER.unwrapKey().orElseThrow());
        assertEquals(TTAspects.AER, host.aspectKey());
        assertEquals(9, host.amount());
        assertTrue(host.releasesOn(Direction.UP));
        IEssentiaStorage storage = new SingleAspectStorage(host).view(Direction.UP);

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(4, storage.extract(AER, 4, transaction));
            assertEquals(5, host.amount);
        }

        assertEquals(9, host.amount);
        assertEquals(TTAspects.AER, host.aspect);
        assertEquals(0, storage.contentRevision());
        assertEquals(0, host.syncs);
    }

    private static Holder<IAspect> aspect(ResourceKey<IAspect> key) {
        Holder<IAspect> holder = mock();
        when(holder.unwrapKey()).thenReturn(Optional.of(key));
        return holder;
    }

    private static final class TestHost implements SingleAspectEssentiaHost {
        private final int capacity;
        private final int insertLimit;
        private ResourceKey<IAspect> aspect;
        private int amount;
        private int syncs;
        private int voided;

        private TestHost(int capacity, int insertLimit) {
            this.capacity = capacity;
            this.insertLimit = insertLimit;
        }

        @Override
        public ResourceKey<IAspect> aspectKey() {
            return aspect;
        }

        @Override
        public ResourceKey<IAspect> aspectFilterKey() {
            return null;
        }

        @Override
        public int amount() {
            return amount;
        }

        @Override
        public int capacity() {
            return capacity;
        }

        @Override
        public com.leclowndu93150.thaumaturge.api.aspect.AspectList getAspects() {
            return com.leclowndu93150.thaumaturge.api.aspect.AspectList.EMPTY;
        }

        @Override
        public boolean receivesOn(Direction face) {
            return true;
        }

        @Override
        public boolean releasesOn(Direction face) {
            return true;
        }

        @Override
        public int storageInsertLimit(int requested) {
            return Math.min(requested, insertLimit);
        }

        @Override
        public void setStorageContents(ResourceKey<IAspect> aspect, int amount) {
            this.aspect = aspect;
            this.amount = amount;
        }

        @Override
        public void onStorageCommitted() {
            syncs++;
        }

        @Override
        public void onStorageVoided(int voided) {
            this.voided += voided;
        }
    }
}
