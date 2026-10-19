package com.leclowndu93150.thaumaturge.content.crucible;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
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
        assertEquals(0.3F, BlockEntityCrucible.fluidSurfaceHeight(0, 0), 0.0001F);
        assertEquals(0.8F, BlockEntityCrucible.fluidSurfaceHeight(1000, 0), 0.0001F);
        assertEquals(0.775F, BlockEntityCrucible.fluidSurfaceHeight(500, 50), 0.0001F);
        assertEquals(0.9999F, BlockEntityCrucible.fluidSurfaceHeight(0, 100), 0.0001F);
        assertEquals(0.9999F, BlockEntityCrucible.fluidSurfaceHeight(1000, 100), 0.0001F);
        assertEquals(1.001F, BlockEntityCrucible.fluidSurfaceHeight(1000, 101), 0.0001F);
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

    @Test
    void heatSyncsOnEitherBoilingTransitionButNotWithoutWater() {
        assertTrue(BlockEntityCrucible.boilingStateChanged(150, 151, 1));
        assertTrue(BlockEntityCrucible.boilingStateChanged(151, 150, 1));
        assertFalse(BlockEntityCrucible.boilingStateChanged(150, 149, 1));
        assertFalse(BlockEntityCrucible.boilingStateChanged(151, 150, 0));
    }

    @Test
    void crucibleParticlePositionsStayOnSurfaceAndRim() {
        BlockPos pos = new BlockPos(12, 34, -5);
        for (int seed = 0; seed < 64; seed++) {
            RandomSource random = RandomSource.create(seed);
            Vec3 froth = CrucibleFx.frothPoint(pos, 0.65F, random);
            assertTrue(froth.x >= pos.getX() + 0.2 && froth.x <= pos.getX() + 0.8);
            assertEquals(pos.getY() + 0.65F, froth.y, 1.0E-6);
            assertTrue(froth.z >= pos.getZ() + 0.2 && froth.z <= pos.getZ() + 0.8);

            Vec3 bubble = CrucibleFx.bubblePoint(pos, 0.65F, random);
            assertTrue(bubble.x >= pos.getX() + 0.21875 && bubble.x <= pos.getX() + 0.78125);
            assertEquals((double) (pos.getY() + 0.65F) + 0.02, bubble.y, 1.0E-6);
            assertTrue(bubble.z >= pos.getZ() + 0.21875 && bubble.z <= pos.getZ() + 0.78125);

            for (Direction side : Direction.Plane.HORIZONTAL) {
                Vec3 overflowFroth = CrucibleFx.rimPoint(pos, side, random);
                assertEquals(pos.getY() + 1.0, overflowFroth.y);
                assertTrue(overflowFroth.x >= pos.getX() - 0.04 && overflowFroth.x <= pos.getX() + 1.04);
                assertTrue(overflowFroth.z >= pos.getZ() - 0.04 && overflowFroth.z <= pos.getZ() + 1.04);
            }
        }
    }
}
