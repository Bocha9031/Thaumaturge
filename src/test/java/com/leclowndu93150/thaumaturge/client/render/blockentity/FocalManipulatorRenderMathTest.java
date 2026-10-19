package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FocalManipulatorRenderMathTest {
    private static final float EPSILON = 0.0001F;

    @Test
    void focusBobAndSpinFollowTheirConfiguredPeriods() {
        assertEquals(0.0F, FocalManipulatorRenderMath.focusBob(0.0F), EPSILON);
        assertEquals(0.035F, FocalManipulatorRenderMath.focusBob(17.5F), EPSILON);
        assertEquals(0.0F, FocalManipulatorRenderMath.focusBob(70.0F), EPSILON);
        assertEquals(0.0F, FocalManipulatorRenderMath.focusSpin(225.0F), EPSILON);
    }

    @Test
    void crystalsOrbitEvenlyAtTheNewRadiusAndStayNearTheFocusHeight() {
        assertEquals(0.36F, FocalManipulatorRenderMath.orbitRadius(), EPSILON);
        assertEquals(0.0F, FocalManipulatorRenderMath.orbitDegrees(0.0F, 0, 4), EPSILON);
        assertEquals(90.0F, FocalManipulatorRenderMath.orbitDegrees(0.0F, 1, 4), EPSILON);
        for (int tick = 0; tick < 100; tick++) {
            for (int index = 0; index < 8; index++) {
                float y = FocalManipulatorRenderMath.crystalHeight(tick, index);
                assertTrue(y >= 1.295F && y <= 1.345F);
            }
        }
    }

    @Test
    void beamProfilesAreStablePerLocationAndRemainInsideTheirMotionBounds() {
        FocalManipulatorRenderMath.Beam expected = FocalManipulatorRenderMath.beam(12345, 2, 0, 40.0F);
        assertEquals(expected, FocalManipulatorRenderMath.beam(12345, 2, 0, 40.0F));
        assertNotEquals(expected, FocalManipulatorRenderMath.beam(12345, 2, 1, 40.0F));
        assertNotEquals(expected, FocalManipulatorRenderMath.beam(54321, 2, 0, 40.0F));
        assertTrue(expected.yawDegrees() >= -36.0F && expected.yawDegrees() <= -8.0F);
        assertTrue(Math.abs(expected.tiltDegrees()) <= 10.0F);
        assertTrue(expected.rollDegrees() >= 0.0F && expected.rollDegrees() < 180.0F);
        assertTrue(expected.length() >= 0.22F && expected.length() < 0.38F);
        assertTrue(expected.tipHalfWidth() >= 0.0455F && expected.tipHalfWidth() <= 0.0945F);
    }
}
