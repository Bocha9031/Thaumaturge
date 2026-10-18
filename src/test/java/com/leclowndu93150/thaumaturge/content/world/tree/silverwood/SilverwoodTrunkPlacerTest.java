package com.leclowndu93150.thaumaturge.content.world.tree.silverwood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class SilverwoodTrunkPlacerTest {
    private static final double TOLERANCE = 1.0E-9;

    @Test
    void crownRadiusHasAFullCanopyCoreAndTapersAtBothEnds() {
        assertEquals(3.2, SilverwoodTrunkPlacer.layerRadius(-3, 4), TOLERANCE);
        assertEquals(3.2, SilverwoodTrunkPlacer.layerRadius(0, 4), TOLERANCE);
        assertEquals(3.2 * Math.sqrt(5.0 / 9.0), SilverwoodTrunkPlacer.layerRadius(-5, 4), TOLERANCE);
        assertEquals(3.2 * Math.sqrt(0.96), SilverwoodTrunkPlacer.layerRadius(1, 4), TOLERANCE);
        assertEquals(1.92, SilverwoodTrunkPlacer.layerRadius(4, 4), TOLERANCE);
    }

}
