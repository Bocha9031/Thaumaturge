package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InfusionMatrixRenderMathTest {
    @Test
    void satelliteMotionIsDeterministicAndBounded() {
        for (int index = 0; index < 8; index++) {
            for (int salt = 0; salt < 3; salt++) {
                float value = InfusionMatrixRenderMath.drift(123.5F, index, salt);
                assertEquals(value, InfusionMatrixRenderMath.drift(123.5F, index, salt));
                assertTrue(value >= -1.0F && value <= 1.0F);
                int turns = InfusionMatrixRenderMath.quarterTurns(index, salt);
                assertTrue(turns >= 0 && turns < 4);
            }
        }
    }

    @Test
    void instabilityRampsFromIdleAndRemainsCapped() {
        assertEquals(1.0F, InfusionMatrixRenderMath.instability(-100.0F, 0));
        assertEquals(InfusionMatrixRenderMath.instability(0.0F, 50), InfusionMatrixRenderMath.instability(0.0F, 100));
        assertTrue(InfusionMatrixRenderMath.instability(-100.0F, 50) <= 8.0F);
    }

    @Test
    void spikeMeshIsFiniteAndHasExpectedShape() {
        float[] spikes = InfusionMatrixRenderMath.buildSpikes();
        assertEquals(InfusionMatrixRenderMath.spikeCount() * InfusionMatrixRenderMath.spikeStride(), spikes.length);
        for (float coordinate : spikes) {
            assertTrue(Float.isFinite(coordinate));
        }
    }
}
