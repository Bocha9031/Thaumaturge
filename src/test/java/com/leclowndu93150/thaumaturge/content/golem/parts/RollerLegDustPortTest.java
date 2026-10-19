package com.leclowndu93150.thaumaturge.content.golem.parts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RollerLegDustPortTest {
    @Test
    void emitsDustOnlyAfterEnoughThreeDimensionalTravel() {
        assertFalse(RollerLegDust.isBrisk(0.25, 0.0, 0.0));
        assertFalse(RollerLegDust.isBrisk(0.0, 0.0, 0.0));
        assertFalse(RollerLegDust.isBrisk(0.0, 0.25, 0.0));
        assertTrue(RollerLegDust.isBrisk(0.2, 0.2, 0.0));
    }
}
