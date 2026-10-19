package com.leclowndu93150.thaumaturge.content.entity.boss;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

final class EntityCultistPortalGreaterTest {
    @Test
    void crateRollsMapUniformlyAcrossTheNinePositionRadius() {
        int[] offsets = new int[9];
        for (int roll = 0; roll < offsets.length; roll++) {
            offsets[roll] = EntityCultistPortalGreater.offsetFromRoll(roll);
        }

        assertArrayEquals(new int[] {-4, -3, -2, -1, 0, 1, 2, 3, 4}, offsets);
    }
}
