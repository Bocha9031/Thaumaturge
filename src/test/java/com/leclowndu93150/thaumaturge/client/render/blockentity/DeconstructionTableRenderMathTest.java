package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DeconstructionTableRenderMathTest {
    @Test
    void itemBobRepeatsAndStaysWithinItsSmallFloatRange() {
        assertEquals(0.0F, DeconstructionTableRenderMath.itemBob(0.0F));
        assertEquals(DeconstructionTableRenderMath.itemBob(12.0F), DeconstructionTableRenderMath.itemBob(102.0F));
        for (int tick = 0; tick < 90; tick++) {
            assertTrue(Math.abs(DeconstructionTableRenderMath.itemBob(tick)) <= 0.045F);
        }
    }

    @Test
    void glowPulsesWithoutBecomingInvisibleOrOpaque() {
        for (int tick = 0; tick < 60; tick++) {
            float alpha = DeconstructionTableRenderMath.glowAlpha(tick);
            assertTrue(alpha >= 0.20F && alpha <= 0.36F);
        }
        assertEquals(DeconstructionTableRenderMath.glowAlpha(0.0F), DeconstructionTableRenderMath.glowAlpha(60.0F));
    }

    @Test
    void aspectTurnsAgainstTheInputItem() {
        assertEquals(-42.0F, DeconstructionTableRenderMath.aspectSpin(42.0F));
    }
}
