package com.leclowndu93150.thaumaturge.client.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PortalAppearanceTest {
    private static final float DELTA = 1.0E-5F;

    @Test
    void openingEasesFromAVisibleSlitToTheFullPortalWidth() {
        assertEquals(0.04F, PortalAppearance.openWidth(0.0F), DELTA);
        assertEquals(0.52F, PortalAppearance.openWidth(25.0F), DELTA);
        assertEquals(1.0F, PortalAppearance.openWidth(50.0F), DELTA);
        assertEquals(1.0F, PortalAppearance.openWidth(80.0F), DELTA);
    }

    @Test
    void damageAddsBreathingAndKeepsOpacityWithinVisibleBounds() {
        assertEquals(0.0F, PortalAppearance.damage(1.0F), DELTA);
        assertEquals(1.0F, PortalAppearance.damage(0.0F), DELTA);
        assertEquals(1.0F, PortalAppearance.opacity(0.0F, 23.0F), DELTA);

        float damagedOpacity = PortalAppearance.opacity(1.0F, 23.0F);
        assertTrue(damagedOpacity >= 0.2F && damagedOpacity < 1.0F);
    }

    @Test
    void hitFlinchCompressesAndWidensWhilePortalPulseSwells() {
        float calmHeight = PortalAppearance.halfHeight(2.9F, 0.0F, 10.0F, 0.0F, 0.0F);
        float flinchingHeight = PortalAppearance.halfHeight(2.9F, 0.0F, 10.0F, 1.0F, 0.0F);
        float calmWidth = PortalAppearance.halfWidth(2.3F, 0.0F, 10.0F, 0.0F, 0.0F);
        float flinchingWidth = PortalAppearance.halfWidth(2.3F, 0.0F, 10.0F, 1.0F, 0.0F);
        float pulsingHeight = PortalAppearance.halfHeight(2.9F, 0.0F, 10.0F, 0.0F, 0.5F);
        float pulsingWidth = PortalAppearance.halfWidth(2.3F, 0.0F, 10.0F, 0.0F, 0.5F);

        assertTrue(flinchingHeight < calmHeight);
        assertTrue(flinchingWidth > calmWidth);
        assertTrue(pulsingHeight > calmHeight);
        assertTrue(pulsingWidth > calmWidth);
    }

    @Test
    void pulseInterpolationStartsAtZeroAndClampsAtItsPeak() {
        assertEquals(0.0F, PortalAppearance.beat(10, 0.0F, 10), DELTA);
        assertEquals(0.5F, PortalAppearance.beat(5, 0.0F, 10), DELTA);
        assertEquals(1.0F, PortalAppearance.beat(1, 1.0F, 10), DELTA);
        assertEquals(0.0F, PortalAppearance.beat(0, 0.0F, 10), DELTA);
    }
}
