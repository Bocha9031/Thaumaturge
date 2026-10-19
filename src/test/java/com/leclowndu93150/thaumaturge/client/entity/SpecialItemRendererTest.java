package com.leclowndu93150.thaumaturge.client.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class SpecialItemRendererTest {
    private static final float EPSILON = 0.0001F;

    @Test
    void rayBudgetTracksGraphicsPreset() {
        assertEquals(5, SpecialItemRenderer.rayCount(true));
        assertEquals(10, SpecialItemRenderer.rayCount(false));
    }

    @Test
    void rayBurstGrowsForTenTicksThenRemainsCapped() {
        assertEquals(0.0F, SpecialItemRenderer.reach(0.0F), EPSILON);
        assertEquals(0.475F, SpecialItemRenderer.reach(5.0F), EPSILON);
        assertEquals(0.95F, SpecialItemRenderer.reach(10.0F), EPSILON);
        assertEquals(0.95F, SpecialItemRenderer.reach(40.0F), EPSILON);
        assertEquals(0.0F, SpecialItemRenderer.reach(-3.0F), EPSILON);
    }

    @Test
    void burstCompletesOneTurnEveryFiveHundredTicks() {
        assertEquals(0.0F, SpecialItemRenderer.spinDegrees(0.0F), EPSILON);
        assertEquals(180.0F, SpecialItemRenderer.spinDegrees(250.0F), EPSILON);
        assertEquals(360.0F, SpecialItemRenderer.spinDegrees(500.0F), EPSILON);
    }
}
