package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EmptyBehaviorPortTest {
    @Test
    void cycleIndexWrapsSafelyEvenAfterTheCounterOverflows() {
        assertEquals(0, EmptyBehavior.cycleIndex(0, 3));
        assertEquals(2, EmptyBehavior.cycleIndex(-1, 3));
        assertEquals(1, EmptyBehavior.cycleIndex(Integer.MIN_VALUE, 3));
    }
}
