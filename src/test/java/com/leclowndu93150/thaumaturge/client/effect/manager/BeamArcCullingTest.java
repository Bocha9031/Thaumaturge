package com.leclowndu93150.thaumaturge.client.effect.manager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BeamArcCullingTest {
    @Test
    void arcsSkipDegenerateLengthAndInvisibleAlpha() {
        assertFalse(BeamManager.shouldRenderArc(0.0, 1.0F));
        assertFalse(BeamManager.shouldRenderArc(1.0E-3 - 1.0E-6, 1.0F));
        assertFalse(BeamManager.shouldRenderArc(1.0E-3, 0.0F));
        assertTrue(BeamManager.shouldRenderArc(1.0E-3, 0.1F));
    }

    @Test
    void beamsSkipDegenerateLengthZeroSizeAndInvisibleOpacity() {
        assertFalse(BeamManager.shouldRenderBeam(0.0, 1.0F, 1.0F));
        assertFalse(BeamManager.shouldRenderBeam(1.0E-3, 0.0F, 1.0F));
        assertFalse(BeamManager.shouldRenderBeam(1.0E-3, 1.0F, 0.0F));
        assertTrue(BeamManager.shouldRenderBeam(1.0E-3, 1.0F, 0.1F));
    }
}
