package com.leclowndu93150.thaumaturge.client.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ThaumometerLensLayoutTest {
    @Test
    void textScaleFitsLongLabelsWithoutBecomingNegative() {
        assertEquals(0.0075F, ThaumometerLensRenderer.fit(0, 0.0075F, 0.62F));
        assertEquals(0.0075F, ThaumometerLensRenderer.fit(60, 0.0075F, 0.62F));
        assertEquals(0.0062F, ThaumometerLensRenderer.fit(100, 0.0075F, 0.62F), 0.000001F);
        assertTrue(ThaumometerLensRenderer.fit(10_000, 0.005F, 0.5F) > 0.0F);
    }

    @Test
    void aspectRowsUseTheTaperedGridCapacity() {
        assertEquals(0, ThaumometerLensRenderer.rowsFor(0));
        assertEquals(1, ThaumometerLensRenderer.rowsFor(5));
        assertEquals(2, ThaumometerLensRenderer.rowsFor(6));
        assertEquals(5, ThaumometerLensRenderer.rowsFor(15));
        assertEquals(5, ThaumometerLensRenderer.rowsFor(16));
    }
}
