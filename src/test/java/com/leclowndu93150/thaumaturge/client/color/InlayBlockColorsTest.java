package com.leclowndu93150.thaumaturge.client.color;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InlayBlockColorsTest {
    @Test
    void chargeTintStartsAtTheUnchargedFloorThenReachesFullBrightness() {
        assertEquals(0.3F, InlayBlockColors.brightness(0));
        assertEquals(0.3F, InlayBlockColors.brightness(-1));
        assertEquals(0.55F, InlayBlockColors.brightness(1));
        assertEquals(1.0F, InlayBlockColors.brightness(15));
    }
}
