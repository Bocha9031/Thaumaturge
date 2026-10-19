package com.leclowndu93150.thaumaturge.client.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BoreDrillFxTest {
    @Test
    void fullGraphicsKeepsBeamThroughSixtyFourBlocks() {
        assertFalse(BoreDrillFx.beyondView(64.0, 0.0, 0.0, false));
        assertTrue(BoreDrillFx.beyondView(64.01, 0.0, 0.0, false));
    }

    @Test
    void fastGraphicsKeepsBeamThroughThirtyTwoBlocks() {
        assertFalse(BoreDrillFx.beyondView(32.0, 0.0, 0.0, true));
        assertTrue(BoreDrillFx.beyondView(32.01, 0.0, 0.0, true));
    }

    @Test
    void cullingUsesThreeDimensionalDistance() {
        assertFalse(BoreDrillFx.beyondView(20.0, 20.0, 0.0, true));
        assertTrue(BoreDrillFx.beyondView(24.0, 24.0, 0.0, true));
    }
}
