package com.leclowndu93150.thaumaturge.content.research.note;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import net.minecraft.util.RandomSource;
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

    @Test
    void distributeRingCapsAtCapacityWithoutRepeatingCells() {
        List<HexGrid.Hex> cells = HexGrid.distributeRing(2, 20, RandomSource.create(17L));

        assertEquals(12, cells.size());
        assertEquals(cells.size(), new HashSet<>(cells).size());
        assertTrue(cells.stream().allMatch(cell -> HexGrid.distance(new HexGrid.Hex(0, 0), cell) == 2));
    }

    @Test
    void distributeRingUsesOriginForZeroRadius() {
        assertEquals(List.of(new HexGrid.Hex(0, 0)), HexGrid.ring(0));
        assertEquals(List.of(new HexGrid.Hex(0, 0)), HexGrid.distributeRing(0, 1, RandomSource.create(17L)));
    }

    @Test
    void distributeRingReturnsNoCellsForNonPositiveEntryCount() {
        assertTrue(HexGrid.distributeRing(2, 0, RandomSource.create(17L)).isEmpty());
        assertTrue(HexGrid.distributeRing(2, -1, RandomSource.create(17L)).isEmpty());
    }
}
