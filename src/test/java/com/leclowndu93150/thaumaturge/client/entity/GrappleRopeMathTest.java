package com.leclowndu93150.thaumaturge.client.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class GrappleRopeMathTest {
    private static final double TOLERANCE = 1.0E-6;

    @Test
    void ropeAlwaysBeginsAtHookAndEndsAtThrower() {
        Vec3 end = new Vec3(3.0, 4.0, -2.0);
        List<Vec3> points = GrappleRopeMath.points(end, 1.0F, 20.0F);

        assertEquals(0.0, points.getFirst().x, TOLERANCE);
        assertEquals(0.0, points.getFirst().y, TOLERANCE);
        assertEquals(0.0, points.getFirst().z, TOLERANCE);
        assertEquals(end.x, points.getLast().x, TOLERANCE);
        assertEquals(end.y, points.getLast().y, TOLERANCE);
        assertEquals(end.z, points.getLast().z, TOLERANCE);
    }

    @Test
    void zeroAmplitudeProducesStraightInterpolatedRope() {
        Vec3 end = new Vec3(2.0, 0.0, 0.0);
        List<Vec3> points = GrappleRopeMath.points(end, 0.0F, 0.0F);

        assertTrue(points.size() >= 3);
        for (int i = 0; i < points.size(); i++) {
            assertEquals(2.0 * i / (points.size() - 1), points.get(i).x, TOLERANCE);
            assertEquals(0.0, points.get(i).y, TOLERANCE);
            assertEquals(0.0, points.get(i).z, TOLERANCE);
        }
    }

    @Test
    void ropeWaveMovesOverTimeAndFadesTowardThrower() {
        Vec3 end = new Vec3(0.0, 0.0, 6.0);
        List<Vec3> first = GrappleRopeMath.points(end, 1.0F, 0.0F);
        List<Vec3> later = GrappleRopeMath.points(end, 1.0F, 5.0F);
        int middle = first.size() / 2;

        assertNotEquals(first.get(middle), later.get(middle));
        assertEquals(end.z, first.getLast().z, TOLERANCE);
        assertEquals(0.0, first.getLast().x, TOLERANCE);
        assertEquals(0.0, first.getLast().y, TOLERANCE);
    }
}
