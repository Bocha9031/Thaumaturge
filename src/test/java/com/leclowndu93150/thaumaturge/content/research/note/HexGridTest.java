package com.leclowndu93150.thaumaturge.content.research.note;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HexGridTest {
    @Test
    void clampToDiskAlwaysReturnsACellWithinTheRequestedRadius() {
        HexGrid.Hex origin = new HexGrid.Hex(0, 0);
        for (int radius = -1; radius <= 5; radius++) {
            for (int q = -30; q <= 30; q++) {
                for (int r = -30; r <= 30; r++) {
                    HexGrid.Hex clamped = HexGrid.clampToDisk(new HexGrid.Hex(q, r), radius);

                    assertTrue(HexGrid.distance(origin, clamped) <= Math.max(0, radius));
                }
            }
        }
    }

    @Test
    void clampToDiskLeavesCellsAlreadyInsideAlone() {
        HexGrid.Hex inside = new HexGrid.Hex(-2, 1);

        assertEquals(inside, HexGrid.clampToDisk(inside, 2));
    }
}
