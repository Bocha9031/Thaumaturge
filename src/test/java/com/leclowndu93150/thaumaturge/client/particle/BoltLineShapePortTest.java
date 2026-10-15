package com.leclowndu93150.thaumaturge.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.client.particle.support.BoltLineShape;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

final class BoltLineShapePortTest {
    @Test
    void emptyAndTinyLinesMatchSourceDegenerateHandling() {
        assertTrue(makeShape(0.0, 0.0, 0.0, 0.0, 0.0, 0.0).isEmpty());
        assertTrue(makeShape(0.0, 0.0, 0.0, 1.0E-6, 0.0, 0.0).isEmpty());
        assertTrue(makeShape(0.0, 0.0, 0.0, Double.POSITIVE_INFINITY, 0.0, 0.0).isEmpty());

        BoltLineShape justAboveThreshold = makeShape(0.0, 0.0, 0.0, 2.0E-6, 0.0, 0.0);
        assertFalse(justAboveThreshold.isEmpty());
        assertEquals(2, justAboveThreshold.sections());
    }

    @Test
    void sectionCountClampsAndInteriorPointsUseOnePrecomputedRandomTrace() {
        RandomSource random = RandomSource.create(73L);
        RandomSource expectedRandom = RandomSource.create(73L);
        double phase = 0.7;
        BoltLineShape shape = new BoltLineShape(0.0, 0.0, 0.0, 1.0, 0.0, 0.0, random, phase);
        assertEquals(3, shape.sections());
        assertEquals(0.0, shape.coordinate(0, 0, 0.4), 0.0);
        assertEquals(1.0, shape.coordinate(shape.sections(), 0, 0.4), 0.0);

        double step = Math.PI / shape.sections();
        double[] divisors = {4.0, 3.0, 2.0};
        for (int index = 1; index < shape.sections(); index++) {
            double fraction = (double) index / shape.sections();
            for (int axis = 0; axis < divisors.length; axis++) {
                float jitter = (expectedRandom.nextFloat() - expectedRandom.nextFloat()) * 0.1F;
                float sway = (float) Math.sin((index * step + phase) / divisors[axis]);
                double expected = (axis == 0 ? fraction : 0.0) + 0.4 * sway + jitter;
                assertEquals(expected, shape.coordinate(index, axis, 0.4), 1.0E-12);
            }
        }
        assertEquals(expectedRandom.nextInt(), random.nextInt());

        BoltLineShape maximum = makeShape(0.0, 0.0, 0.0, 1000.0, 0.0, 0.0);
        assertEquals(512, maximum.sections());
    }

    private static BoltLineShape makeShape(
            double x, double y, double z, double targetX, double targetY, double targetZ) {
        return new BoltLineShape(x, y, z, targetX, targetY, targetZ, RandomSource.create(1L), 0.0);
    }
}
