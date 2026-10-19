package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class JarRendererFluidUvTest {
    @Test
    void fluidSideTextureRunsFromTheBottomUp() {
        assertEquals(1.0F - JarRenderer.FLUID_BASE_Y, JarRenderer.fluidSideBottomV());
        assertEquals(JarRenderer.fluidSideBottomV(), JarRenderer.fluidSideTopV(JarRenderer.FLUID_BASE_Y));
        assertEquals(
                1.0F - (JarRenderer.FLUID_BASE_Y + JarRenderer.FLUID_MAX_HEIGHT),
                JarRenderer.fluidSideTopV(JarRenderer.FLUID_BASE_Y + JarRenderer.FLUID_MAX_HEIGHT));
    }
}
