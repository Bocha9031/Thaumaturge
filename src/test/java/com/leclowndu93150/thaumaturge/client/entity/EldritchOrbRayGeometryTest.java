package com.leclowndu93150.thaumaturge.client.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EldritchOrbRayGeometryTest {
    @Test
    void precomputedTipsAreFiniteAndStayWithinTheRayEnvelope() {
        float[] tips = EldritchOrbRayGeometry.tipsForTest();

        assertEquals(16 * 3 * 3, tips.length);
        for (int offset = 0; offset < tips.length; offset += 3) {
            float x = tips[offset];
            float y = tips[offset + 1];
            float z = tips[offset + 2];
            float radius = (float) Math.sqrt(x * x + y * y + z * z);

            assertTrue(Float.isFinite(radius));
            assertTrue(radius >= 0.15F);
            assertTrue(radius <= 1.1F);
        }
    }

    @Test
    void testRayTipsAreDefensiveCopies() {
        float[] first = EldritchOrbRayGeometry.tipsForTest();
        float expected = first[0];
        first[0] = Float.NaN;

        assertEquals(expected, EldritchOrbRayGeometry.tipsForTest()[0]);
    }
}
