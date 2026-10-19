package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.Mth;
import org.junit.jupiter.api.Test;

class JarBrainRenderMathTest {
    @Test
    void animationClockWrapsWithoutDependingOnTheLocalPlayer() {
        assertEquals(12.5F, JarBrainRenderMath.age(12L, 0.5F));
        assertEquals(12.5F, JarBrainRenderMath.age(JarBrainRenderMath.CLOCK_WRAP + 12L, 0.5F));
    }

    @Test
    void bobIsBoundedAndPositionSeeded() {
        float first = JarBrainRenderMath.bobOffset(40L, 0.25F, 1234L);
        assertEquals(first, JarBrainRenderMath.bobOffset(40L, 0.25F, 1234L));
        assertNotEquals(first, JarBrainRenderMath.bobOffset(40L, 0.25F, 1235L));
        assertTrue(Math.abs(first) <= JarBrainRenderMath.BOB_AMPLITUDE);
    }

    @Test
    void yawInterpolationTakesTheShortPathAcrossTheWrap() {
        float interpolated = JarBrainRenderMath.interpolateYaw(Mth.DEG_TO_RAD * 179.0F, Mth.DEG_TO_RAD * -179.0F, 0.5F);
        assertEquals(Mth.PI, Math.abs(interpolated), 0.001F);
    }

    @Test
    void xpResponseScalesWithStoredExperienceAndClampsInvalidValues() {
        assertEquals(0.0F, JarBrainRenderMath.xpResponse(1.25F, 0, 2000));
        assertEquals(Mth.sin(1.0F / JarBrainRenderMath.XP_PERIOD), JarBrainRenderMath.xpResponse(1.0F, 2000, 2000));
        assertEquals(Mth.sin(1.0F / JarBrainRenderMath.XP_PERIOD), JarBrainRenderMath.xpResponse(1.0F, 3000, 2000));
    }

    @Test
    void visibleExperienceFillStaysWithinTheJarCapacity() {
        assertEquals(0, JarBrainRenderMath.visibleXp(-5, 2000));
        assertEquals(650, JarBrainRenderMath.visibleXp(650, 2000));
        assertEquals(2000, JarBrainRenderMath.visibleXp(2500, 2000));
        assertEquals(0, JarBrainRenderMath.visibleXp(10, 0));
    }
}
