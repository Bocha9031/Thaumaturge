package com.leclowndu93150.thaumaturge.content.misc.nitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BlockNitorShapeTest {
    @Test
    void selectionShapeMatchesTheCenteredSixPixelModel() {
        var bounds = BlockNitor.SHAPE.bounds();

        assertEquals(5.0 / 16.0, bounds.minX, 1.0E-6);
        assertEquals(5.0 / 16.0, bounds.minY, 1.0E-6);
        assertEquals(5.0 / 16.0, bounds.minZ, 1.0E-6);
        assertEquals(11.0 / 16.0, bounds.maxX, 1.0E-6);
        assertEquals(11.0 / 16.0, bounds.maxY, 1.0E-6);
        assertEquals(11.0 / 16.0, bounds.maxZ, 1.0E-6);
    }
}
