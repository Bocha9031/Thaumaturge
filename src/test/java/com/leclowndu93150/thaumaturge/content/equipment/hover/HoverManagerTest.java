package com.leclowndu93150.thaumaturge.content.equipment.hover;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HoverManagerTest {
    @Test
    void hoverFuelTicksConsumeExactlyAtTheConfiguredInterval() {
        HoverManager.ChargeTick beforeBoundary = HoverManager.advanceCharge(HoverManager.FUEL_INTERVAL_TICKS - 2);
        assertEquals(HoverManager.FUEL_INTERVAL_TICKS - 1, beforeBoundary.elapsedTicks());
        assertFalse(beforeBoundary.consumeFuel());

        HoverManager.ChargeTick boundary = HoverManager.advanceCharge(HoverManager.FUEL_INTERVAL_TICKS - 1);
        assertEquals(0, boundary.elapsedTicks());
        assertTrue(boundary.consumeFuel());
    }
}
