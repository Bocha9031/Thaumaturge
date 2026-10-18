package com.leclowndu93150.thaumaturge.content.crucible;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CruciblePortTest {
    @Test
    void thrownIntakeProcessesHalfTheStackRoundedUp() {
        assertEquals(1, BlockEntityCrucible.thrownShare(1));
        assertEquals(2, BlockEntityCrucible.thrownShare(3));
        assertEquals(32, BlockEntityCrucible.thrownShare(64));
        assertEquals(0, BlockEntityCrucible.thrownShare(0));
        assertEquals(0, BlockEntityCrucible.thrownShare(-4));
    }

    @Test
    void comparatorOutputStaysWithinVanillaSignalRange() {
        assertEquals(0, BlockCrucible.comparatorSignal(0));
        assertEquals(1, BlockCrucible.comparatorSignal(1));
        assertEquals(8, BlockCrucible.comparatorSignal(50));
        assertEquals(15, BlockCrucible.comparatorSignal(100));
        assertEquals(15, BlockCrucible.comparatorSignal(150));
    }

    @Test
    void crucibleSurfaceTracksWaterAndEssentiaAndCapsOverflow() {
        assertEquals(0.25F, BlockEntityCrucible.fluidSurfaceHeight(0, 0));
        assertEquals(0.75F, BlockEntityCrucible.fluidSurfaceHeight(1000, 0));
        assertEquals(0.95F, BlockEntityCrucible.fluidSurfaceHeight(1000, 100));
        assertEquals(1.01F, BlockEntityCrucible.fluidSurfaceHeight(1000, 101));
    }

    @Test
    void passivePhysicalFluxRequiresBoilingHeat() {
        assertFalse(BlockEntityCrucible.ventsPhysicalFlux(150));
        assertTrue(BlockEntityCrucible.ventsPhysicalFlux(151));
    }

    @Test
    void crucibleOnlyBoilsAboveThresholdWithWater() {
        assertFalse(BlockEntityCrucible.isBoiling(150, 1));
        assertTrue(BlockEntityCrucible.isBoiling(151, 1));
        assertFalse(BlockEntityCrucible.isBoiling(200, 0));
    }
}
