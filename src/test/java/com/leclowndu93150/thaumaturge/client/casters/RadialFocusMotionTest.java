package com.leclowndu93150.thaumaturge.client.casters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RadialFocusMotionTest {
    private static final long MAX_FRAME_DELTA = 100L;

    @Test
    void normalFrameUsesItsFullElapsedTime() {
        assertEquals(0.5F, RadialFocusMotion.change(50L, 0L, 100L, MAX_FRAME_DELTA));
    }

    @Test
    void longFrameCannotSkipThePickerTransition() {
        assertEquals(1.0F, RadialFocusMotion.change(500L, 0L, 100L, MAX_FRAME_DELTA));
    }

    @Test
    void backwardsClockDoesNotReverseTheAnimation() {
        assertEquals(0.0F, RadialFocusMotion.change(40L, 50L, 100L, MAX_FRAME_DELTA));
    }

    @Test
    void invalidDurationOrClampProducesNoProgress() {
        assertEquals(0.0F, RadialFocusMotion.change(40L, 0L, 0L, MAX_FRAME_DELTA));
        assertEquals(0.0F, RadialFocusMotion.change(40L, 0L, 100L, 0L));
    }

    @Test
    void easingStartsGentlyAndFinishesAtTheTarget() {
        assertEquals(0.0F, RadialFocusMotion.easeOutCubic(0.0F));
        assertEquals(0.875F, RadialFocusMotion.easeOutCubic(0.5F));
        assertEquals(1.0F, RadialFocusMotion.easeOutCubic(1.0F));
    }

    @Test
    void easingClampsOutOfRangeValues() {
        assertEquals(0.0F, RadialFocusMotion.easeOutCubic(-1.0F));
        assertEquals(1.0F, RadialFocusMotion.easeOutCubic(2.0F));
    }
}
