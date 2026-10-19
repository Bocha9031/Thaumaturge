package com.leclowndu93150.thaumaturge.client.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TurretCrossbowMotionTest {
    private static final float TOLERANCE = 1.0E-6F;

    @Test
    void loadingAndFiringAnimationsSnapToACompleteCycle() {
        assertEquals(0.0F, TurretCrossbowMotion.snap(0.0F), TOLERANCE);
        assertEquals(1.0F, TurretCrossbowMotion.snap(0.25F), TOLERANCE);
        assertEquals(0.0F, TurretCrossbowMotion.snap(1.0F), TOLERANCE);
        assertEquals(0.0F, TurretCrossbowMotion.snap(2.0F), TOLERANCE);
        assertEquals(0.0F, TurretCrossbowMotion.snap(-1.0F), TOLERANCE);
    }

    @Test
    void hurtTintAndJiggleFadeAcrossTheFullDamageWindow() {
        assertEquals(1.0F, TurretCrossbowMotion.hurtFade(10, 0.0F, 10), TOLERANCE);
        assertEquals(0.5F, TurretCrossbowMotion.hurtFade(5, 0.0F, 10), TOLERANCE);
        assertEquals(0.0F, TurretCrossbowMotion.hurtFade(0, 0.0F, 10), TOLERANCE);
        assertEquals(0.0F, TurretCrossbowMotion.hurtFade(5, 0.0F, 0), TOLERANCE);
    }
}
