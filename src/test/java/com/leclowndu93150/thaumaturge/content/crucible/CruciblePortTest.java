package com.leclowndu93150.thaumaturge.content.crucible;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CruciblePortTest {
    @Test
    void thrownIntakeProcessesHalfTheStackRoundedUp() {
        assertEquals(1, BlockEntityCrucible.thrownShare(1));
        assertEquals(2, BlockEntityCrucible.thrownShare(3));
        assertEquals(32, BlockEntityCrucible.thrownShare(64));
        assertEquals(0, BlockEntityCrucible.thrownShare(0));
        assertEquals(0, BlockEntityCrucible.thrownShare(-4));
    }

    @Test
    void comparatorOutputStaysWithinVanillaSignalRange() {
        assertEquals(0, BlockCrucible.comparatorSignal(0));
        assertEquals(1, BlockCrucible.comparatorSignal(1));
        assertEquals(8, BlockCrucible.comparatorSignal(50));
        assertEquals(15, BlockCrucible.comparatorSignal(100));
        assertEquals(15, BlockCrucible.comparatorSignal(150));
    }
}
