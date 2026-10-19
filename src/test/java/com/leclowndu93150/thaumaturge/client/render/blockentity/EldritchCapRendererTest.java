package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class EldritchCapRendererTest {
    @Test
    void capsPersistedEyeCountsAtTheRenderedMaximum() {
        assertEquals(0, EldritchCapRenderer.clampEyeCount(0));
        assertEquals(4, EldritchCapRenderer.clampEyeCount(4));
        assertEquals(4, EldritchCapRenderer.clampEyeCount(9));
    }
}
