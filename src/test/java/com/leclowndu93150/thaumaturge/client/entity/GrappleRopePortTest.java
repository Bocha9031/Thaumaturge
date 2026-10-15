package com.leclowndu93150.thaumaturge.client.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class GrappleRopePortTest {
    private static final int SIDES = 6;
    private static final double RADIUS = 0.022;
    private static final double TOLERANCE = 1.0E-5;
    private static final double FLOAT_FRAME_TOLERANCE = 2.0E-5;

    @Test
    void usesSourceColdStartNormalForHorizontalAndVerticalRopes() {
        Vector3f[] horizontal = GrappleRenderer.buildRings(List.of(Vec3.ZERO, new Vec3(1.0, 0.0, 0.0)));
        assertEquals(SIDES * 2, horizontal.length);
        assertVector(offset(horizontal[0], Vec3.ZERO), new double[] {0.0, RADIUS, 0.0}, TOLERANCE);

        Vector3f[] vertical = GrappleRenderer.buildRings(List.of(Vec3.ZERO, new Vec3(0.0, 1.0, 0.0)));
        assertVector(offset(vertical[0], Vec3.ZERO), new double[] {RADIUS, 0.0, 0.0}, TOLERANCE);
    }

    @Test
    void transportedFramesStayOrthonormalAndContinuousNearVertical() {
        List<Vec3> points = List.of(
                Vec3.ZERO,
                new Vec3(0.1, 1.0, 0.0),
                new Vec3(0.2, 2.0, 0.05),
                new Vec3(0.2, 3.0, 0.1),
                new Vec3(0.1, 4.0, 0.1),
                new Vec3(0.0, 5.0, 0.0));
        Vector3f[] rings = GrappleRenderer.buildRings(points);
        assertEquals(points.size() * SIDES, rings.length);

        double[] previousNormal = null;
        for (int index = 0; index < points.size(); index++) {
            double[] normalOffset = offset(rings[index * SIDES], points.get(index));
            assertEquals(RADIUS, length(normalOffset), TOLERANCE);
            double[] normal = scale(normalOffset, 1.0 / RADIUS);
            if (previousNormal != null) {
                assertTrue(dot(previousNormal, normal) > 0.9, "transported frame should not flip between samples");
            }
            previousNormal = normal;

            double[] nextOffset = offset(rings[index * SIDES + 1], points.get(index));
            double[] binormalOffset = new double[3];
            double sin60 = Math.sin(Math.PI / 3.0);
            for (int axis = 0; axis < 3; axis++) {
                binormalOffset[axis] = (nextOffset[axis] / RADIUS - normal[axis] * 0.5) / sin60;
            }
            double[] tangent = tangentAt(points, index);
            assertTrue(Math.abs(dot(normal, tangent)) < TOLERANCE);
            assertTrue(Math.abs(dot(binormalOffset, tangent)) < FLOAT_FRAME_TOLERANCE);
            assertTrue(Math.abs(dot(normal, binormalOffset)) < TOLERANCE);
            assertEquals(1.0, length(binormalOffset), TOLERANCE);
        }
    }

    @Test
    void degenerateSegmentsRetainFiniteSourceFrame() {
        Vector3f[] rings = GrappleRenderer.buildRings(List.of(Vec3.ZERO, Vec3.ZERO, Vec3.ZERO));
        assertEquals(3 * SIDES, rings.length);
        for (int index = 0; index < 3; index++) {
            double[] offset = offset(rings[index * SIDES], Vec3.ZERO);
            assertEquals(RADIUS, length(offset), TOLERANCE);
            assertTrue(Double.isFinite(offset[0]) && Double.isFinite(offset[1]) && Double.isFinite(offset[2]));
        }

        List<Vec3> closedPoints = List.of(Vec3.ZERO, new Vec3(1.0, 0.0, 0.0), new Vec3(1.0, 1.0, 0.0), Vec3.ZERO);
        Vector3f[] closedRings = GrappleRenderer.buildRings(closedPoints);
        assertEquals(closedPoints.size() * SIDES, closedRings.length);
        for (int index = 0; index < closedPoints.size(); index++) {
            for (int side = 0; side < SIDES; side++) {
                double[] offset = offset(closedRings[index * SIDES + side], closedPoints.get(index));
                assertEquals(RADIUS, length(offset), TOLERANCE);
                assertTrue(Double.isFinite(offset[0]) && Double.isFinite(offset[1]) && Double.isFinite(offset[2]));
            }
        }
    }

    private static double[] tangentAt(List<Vec3> points, int index) {
        Vec3 before = points.get(Math.max(0, index - 1));
        Vec3 after = points.get(Math.min(points.size() - 1, index + 1));
        Vec3 delta = after.subtract(before).normalize();
        return new double[] {delta.x, delta.y, delta.z};
    }

    private static double[] offset(Vector3f vertex, Vec3 center) {
        return new double[] {vertex.x() - center.x, vertex.y() - center.y, vertex.z() - center.z};
    }

    private static double[] scale(double[] vector, double scale) {
        return new double[] {vector[0] * scale, vector[1] * scale, vector[2] * scale};
    }

    private static double dot(double[] first, double[] second) {
        return first[0] * second[0] + first[1] * second[1] + first[2] * second[2];
    }

    private static double length(double[] vector) {
        return Math.sqrt(dot(vector, vector));
    }

    private static void assertVector(double[] actual, double[] expected, double tolerance) {
        for (int axis = 0; axis < 3; axis++) {
            assertEquals(expected[axis], actual[axis], tolerance);
        }
    }
}
