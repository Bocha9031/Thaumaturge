package com.leclowndu93150.thaumaturge.content.golem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class GolemFlightSteeringPortTest {
    @Test
    void normalizedSteeringUsesMainAccelerationAndSpeedScale() {
        assertEquals(
                new Vec3(0.0175, 0.0, 0.0),
                GolemFlightSteering.desiredDelta(new Vec3(3.0, 0.0, 0.0), 1.0));
        assertEquals(
                new Vec3(0.0, 0.0015, 0.0),
                GolemFlightSteering.desiredDelta(new Vec3(0.0, 5.0, 0.0), 1.0));
        assertEquals(
                new Vec3(0.00875, 0.0, 0.0),
                GolemFlightSteering.desiredDelta(new Vec3(1.0, 0.0, 0.0), 0.5));
    }
}
