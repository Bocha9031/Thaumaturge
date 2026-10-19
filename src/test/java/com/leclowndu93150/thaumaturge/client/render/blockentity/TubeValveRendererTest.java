package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TubeValveRendererTest {
    @Test
    void valveHeadSinksOnlyAsItClosesAndClampsAtTheTravelLimit() {
        assertEquals(0.0F, TubeValveRenderer.sinkOffset(0.0F), 1.0E-6F);
        assertEquals(-0.055F, TubeValveRenderer.sinkOffset(180.0F), 1.0E-6F);
        assertEquals(-0.11F, TubeValveRenderer.sinkOffset(360.0F), 1.0E-6F);
        assertEquals(-0.11F, TubeValveRenderer.sinkOffset(720.0F), 1.0E-6F);
        assertEquals(0.0F, TubeValveRenderer.sinkOffset(-10.0F), 1.0E-6F);
    }
}
