package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FluxScrubberRenderMathTest {
    @Test
    void pulseHasSmoothZeroAndPeakExtremes() {
        assertEquals(0.0F, FluxScrubberRenderMath.bob(0L, 0.0F, 0L));
        assertEquals(FluxScrubberRenderMath.PULSE_DEPTH, FluxScrubberRenderMath.bob(70L, 0.0F, 0L));
        assertEquals(0.0F, FluxScrubberRenderMath.bob(140L, 0.0F, 0L));
    }

    @Test
    void pulseRepeatsAndDifferentBlocksReceiveDifferentPhases() {
        float first = FluxScrubberRenderMath.bob(23L, 0.5F, 27L);
        assertEquals(first, FluxScrubberRenderMath.bob(23L + 140L, 0.5F, 27L));
        assertNotEquals(first, FluxScrubberRenderMath.bob(23L, 0.5F, 28L));
    }

    @Test
    void pulseStaysWithinConfiguredTravel() {
        for (long tick = 0; tick < 2L * 140L; tick++) {
            float bob = FluxScrubberRenderMath.bob(tick, 0.25F, 731L);
            assertTrue(bob >= 0.0F && bob <= FluxScrubberRenderMath.PULSE_DEPTH);
        }
    }
}
