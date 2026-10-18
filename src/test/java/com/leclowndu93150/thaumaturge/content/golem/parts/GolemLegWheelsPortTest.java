package com.leclowndu93150.thaumaturge.content.golem.parts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GolemLegWheelsPortTest {
    @Test
    void emitsDustOnlyAfterEnoughHorizontalTravel() {
        assertFalse(GolemLegWheels.isBrisk(0.25, 0.0));
        assertFalse(GolemLegWheels.isBrisk(0.0, 0.0));
        assertTrue(GolemLegWheels.isBrisk(0.2, 0.2));
    }
}
