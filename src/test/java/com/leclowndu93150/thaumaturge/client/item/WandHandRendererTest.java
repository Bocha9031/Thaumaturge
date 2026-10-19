package com.leclowndu93150.thaumaturge.client.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WandHandRendererTest {
    @Test
    void usePoseTransitionsSmoothlyIntoItsSettledAngle() {
        assertEquals(0.0F, WandHandRenderer.useSettle(0.0F));
        assertEquals(0.5F, WandHandRenderer.useSettle(2.5F));
        assertEquals(1.0F, WandHandRenderer.useSettle(5.0F));
        assertEquals(1.0F, WandHandRenderer.useSettle(20.0F));
        assertEquals(0.0F, WandHandRenderer.useSettle(-1.0F));
    }
}
