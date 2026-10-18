package com.leclowndu93150.thaumaturge.content.world.tree.crown;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class CrownFoliagePlacerTest {
    @Test
    void crownLayersUseNarrowOuterAndBroadInnerProfiles() {
        assertEquals(5, countLayer(true));
        assertEquals(21, countLayer(false));
    }

    private static int countLayer(boolean plus) {
        int count = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (CrownFoliagePlacer.inLayer(plus, dx, dz)) {
                    count++;
                }
            }
        }
        return count;
    }
}
