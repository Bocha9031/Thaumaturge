package com.leclowndu93150.thaumaturge.client.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

final class FluxRiftGeometryTest {
    private static final double TOLERANCE = 1.0E-5;

    @Test
    void stableRiftKeepsItsCenterlineAndWidths() {
        List<Vec3> outline = List.of(new Vec3(0.0, 0.0, 0.0), new Vec3(1.0, 2.0, 0.0), new Vec3(2.0, 3.0, 1.0));
        FluxRiftGeometry.Tube tube = FluxRiftGeometry.build(outline, List.of(0.5F, 0.75F, 1.0F), 50.0F, 120.0F);

        assertEquals(outline.size(), tube.count());
        for (int i = 0; i < outline.size(); i++) {
            assertEquals(outline.get(i).x, tube.path()[i * 3], TOLERANCE);
            assertEquals(outline.get(i).y, tube.path()[i * 3 + 1], TOLERANCE);
            assertEquals(outline.get(i).z, tube.path()[i * 3 + 2], TOLERANCE);
        }
        assertEquals(0.5F, tube.radii()[0], TOLERANCE);
        assertEquals(0.75F, tube.radii()[1], TOLERANCE);
        assertEquals(1.0F, tube.radii()[2], TOLERANCE);
    }

    @Test
    void unstableRiftMovesAndPulsesWithinConfiguredBounds() {
        FluxRiftGeometry.Tube tube = FluxRiftGeometry.build(
                List.of(new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 1.0, 0.0)), List.of(1.0F, 1.0F), 0.0F, 10.0F);

        assertTrue(Math.abs(tube.path()[0]) <= 0.035F);
        assertTrue(tube.radii()[0] >= 0.82F && tube.radii()[0] <= 1.18F);
    }

    @Test
    void tubeFramesStayFiniteAndPerpendicularOnVerticalAndDegenerateSegments() {
        for (List<Vec3> outline : List.of(
                List.of(new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 2.0, 0.0), new Vec3(0.0, 4.0, 0.0)),
                List.of(new Vec3(1.0, 1.0, 1.0), new Vec3(1.0, 1.0, 1.0), new Vec3(1.0, 1.0, 1.0)))) {
            FluxRiftGeometry.Tube tube = FluxRiftGeometry.build(outline, List.of(1.0F, 1.0F, 1.0F), 25.0F, 0.0F);
            for (int point = 0; point < tube.count(); point++) {
                double[] tangent = tangent(tube, point);
                for (int side = 0; side < 6; side++) {
                    int at = (point * 6 + side) * 3;
                    double x = tube.rings()[at];
                    double y = tube.rings()[at + 1];
                    double z = tube.rings()[at + 2];
                    assertTrue(Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z));
                    assertEquals(1.0, Math.sqrt(x * x + y * y + z * z), TOLERANCE);
                    assertEquals(0.0, x * tangent[0] + y * tangent[1] + z * tangent[2], TOLERANCE);
                }
            }
        }
    }

    private static double[] tangent(FluxRiftGeometry.Tube tube, int point) {
        int before = Math.max(0, point - 1) * 3;
        int after = Math.min(tube.count() - 1, point + 1) * 3;
        double x = tube.path()[after] - tube.path()[before];
        double y = tube.path()[after + 1] - tube.path()[before + 1];
        double z = tube.path()[after + 2] - tube.path()[before + 2];
        double length = Math.sqrt(x * x + y * y + z * z);
        return length < TOLERANCE ? new double[] {0.0, 1.0, 0.0} : new double[] {x / length, y / length, z / length};
    }
}
