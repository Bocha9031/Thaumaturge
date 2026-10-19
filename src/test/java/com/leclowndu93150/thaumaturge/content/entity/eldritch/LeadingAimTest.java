package com.leclowndu93150.thaumaturge.content.entity.eldritch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class LeadingAimTest {
    private static final double DELTA = 1.0E-6;

    @Test
    void aimsAtTheTargetMidpointAndLeadsByOneTickByDefault() {
        Vec3 aim = LeadingAim.direction(
                new Vec3(1.0, 2.0, 3.0), new Vec3(10.0, 4.0, 7.0), 2.0, new Vec3(0.5, 0.25, -1.0), 1.0);

        assertEquals(9.5, aim.x, DELTA);
        assertEquals(3.25, aim.y, DELTA);
        assertEquals(3.0, aim.z, DELTA);
    }

    @Test
    void explicitLeadTimeScalesTheTargetMotion() {
        Vec3 aim = LeadingAim.direction(Vec3.ZERO, new Vec3(4.0, 5.0, 6.0), 4.0, new Vec3(2.0, -1.0, 0.5), 2.5);

        assertEquals(9.0, aim.x, DELTA);
        assertEquals(4.5, aim.y, DELTA);
        assertEquals(7.25, aim.z, DELTA);
    }
}
