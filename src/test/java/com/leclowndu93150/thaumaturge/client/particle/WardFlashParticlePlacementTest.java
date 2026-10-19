package com.leclowndu93150.thaumaturge.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

final class WardFlashParticlePlacementTest {
    @Test
    void faceNormalIsOffsetOutsideTheBlockAndTangentialAxesStayInset() {
        RandomSource random = RandomSource.create(11L);

        assertEquals(1.005, WardFlashParticle.placeAlong(1, 0.5, random), 0.0);
        assertEquals(-0.005, WardFlashParticle.placeAlong(-1, 0.5, random), 0.0);
        assertEquals(0.1, WardFlashParticle.placeAlong(0, 0.0, random), 0.0);
        assertEquals(0.9, WardFlashParticle.placeAlong(0, 1.0, random), 0.0);
    }

    @Test
    void flashSizeRemainsWithinTheIntendedVisualRange() {
        assertEquals(0.7F, WardFlashParticle.sizeForDeviation(0.0), 0.0F);
        assertEquals(0.2F, WardFlashParticle.sizeForDeviation(-100.0), 0.0F);
        assertEquals(1.2F, WardFlashParticle.sizeForDeviation(100.0), 0.0F);
        assertTrue(WardFlashParticle.sizeForDeviation(0.5) <= 1.2F);
    }
}
