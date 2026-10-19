package com.leclowndu93150.thaumaturge.content.golem.parts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class RollerLegDustPortTest {
    @Test
    void emitsDustOnlyAfterEnoughThreeDimensionalTravel() {
        assertFalse(RollerLegDust.isBrisk(0.25, 0.0, 0.0));
        assertFalse(RollerLegDust.isBrisk(0.0, 0.0, 0.0));
        assertFalse(RollerLegDust.isBrisk(0.0, 0.25, 0.0));
        assertTrue(RollerLegDust.isBrisk(0.2, 0.2, 0.0));
    }

    @Test
    void samplesTheSurfaceBlockUnderThinGroundCover() {
        assertEquals(new BlockPos(4, 1, -2), RollerLegDust.groundPosition(4.5, 1.0625, -1.5));
    }
}
