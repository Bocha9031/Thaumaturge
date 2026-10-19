package com.leclowndu93150.thaumaturge.client.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

final class CultistClericRenderMathTest {
    private static final float EPSILON = 0.0001F;

    @Test
    void ritualistHoverUsesAnEntityPhaseAndRepeatsEverySeventyTicks() {
        float first = CultistClericRenderMath.bob(13.0F, 24);
        assertEquals(first, CultistClericRenderMath.bob(83.0F, 24), EPSILON);
        assertNotEquals(first, CultistClericRenderMath.bob(13.0F, 25));
        assertTrue(first >= 0.09F && first <= 0.31F);
    }

    @Test
    void ritualBeamFromFocusTerminatesAtTheRaisedClericHead() {
        Vec3 position = new Vec3(10.5, 64.0, -3.25);
        Vec3 focus = new Vec3(14.5, 68.0, 2.75);
        float lineStartY = CultistClericRenderMath.lineStartY(1.95F, 0.24F);
        Vec3 endpoint = focus.add(CultistClericRenderMath.fromFocusToHead(position, focus, lineStartY));

        assertEquals(position.x, endpoint.x, EPSILON);
        assertEquals(position.y + lineStartY, endpoint.y, EPSILON);
        assertEquals(position.z, endpoint.z, EPSILON);
    }

    @Test
    void ritualBeamDrawsOutOverTenTicksAndClampsBothEnds() {
        assertEquals(0.0F, CultistClericRenderMath.fade(-1.0F), EPSILON);
        assertEquals(0.5F, CultistClericRenderMath.fade(5.0F), EPSILON);
        assertEquals(1.0F, CultistClericRenderMath.fade(10.0F), EPSILON);
        assertEquals(1.0F, CultistClericRenderMath.fade(24.0F), EPSILON);
    }
}
