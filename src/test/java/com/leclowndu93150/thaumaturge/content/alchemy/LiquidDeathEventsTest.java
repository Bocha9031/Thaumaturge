package com.leclowndu93150.thaumaturge.content.alchemy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class LiquidDeathEventsTest {
    @Test
    void crystalCountScalesWithTotalAspectPointsAndAlwaysHasOneBaseDrop() {
        assertEquals(0, LiquidDeathEvents.rollCrystalCount(0, RandomSource.create(0L)));
        assertEquals(1, LiquidDeathEvents.rollCrystalCount(9, RandomSource.create(0L)));

        int count = LiquidDeathEvents.rollCrystalCount(100, RandomSource.create(0L));
        assertTrue(count >= 1 && count <= 11);
    }
}
