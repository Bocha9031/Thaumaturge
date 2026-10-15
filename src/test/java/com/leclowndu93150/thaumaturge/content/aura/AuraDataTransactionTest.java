package com.leclowndu93150.thaumaturge.content.aura;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.leclowndu93150.thaumaturge.api.transaction.Transaction;
import org.junit.jupiter.api.Test;

class AuraDataTransactionTest {
    @Test
    void rollbackRestoresVisAndCommittedChildRemainsRollbackableByRoot() {
        AuraData data = new AuraData((short) 100, 70.0F, 5.0F);

        try (Transaction root = Transaction.openRoot()) {
            data.setVis(60.0F, root);
            try (Transaction child = Transaction.open(root)) {
                data.setVis(40.0F, child);
                child.commit();
            }
            assertEquals(40.0F, data.getVis());
        }
        assertEquals(70.0F, data.getVis());

        try (Transaction root = Transaction.openRoot()) {
            data.setVis(55.0F, root);
            root.commit();
        }
        assertEquals(55.0F, data.getVis());
    }
}
