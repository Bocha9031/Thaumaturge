package com.leclowndu93150.thaumaturge.client.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WandLightMathTest {
    @Test
    void focusGlowAndRuneLightUseBothPackedLightChannels() {
        assertPackedChannel(WandLightMath.focus(0.0F));
        assertPackedChannel(WandLightMath.glow(0.0F));
        assertEquals(236, WandLightMath.smoothPacked(236) & 0xFFFF);
        assertEquals(236, WandLightMath.smoothPacked(236) >>> 16);
    }

    @Test
    void focusAndRodGlowStayWithinTheirDesignedSmoothLightRanges() {
        for (int tick = 0; tick < 200; tick++) {
            int focus = WandLightMath.focus(tick);
            int glow = WandLightMath.glow(tick);
            assertTrue((focus & 0xFFFF) >= 208 && (focus & 0xFFFF) <= 228);
            assertTrue((glow & 0xFFFF) >= 221 && (glow & 0xFFFF) <= 231);
            assertPackedChannel(focus);
            assertPackedChannel(glow);
        }
    }

    private static void assertPackedChannel(int packed) {
        assertEquals(packed & 0xFFFF, packed >>> 16);
    }
}
