package com.leclowndu93150.thaumaturge.client.effect.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class BeamArcOrientationTest {
    @Test
    void horizontalRibbonUsesPerpendicularToArcHeading() {
        Vec3 side = BeamManager.arcHorizontalSide(new Vec3(3.0, 4.0, 4.0));

        assertEquals(-0.8, side.x, 1.0E-9);
        assertEquals(0.0, side.y, 1.0E-9);
        assertEquals(0.6, side.z, 1.0E-9);
    }

    @Test
    void nearVerticalArcUsesStableHorizontalFallback() {
        Vec3 side = BeamManager.arcHorizontalSide(new Vec3(0.0, 5.0, 0.0005));

        assertEquals(1.0, side.x, 1.0E-9);
        assertEquals(0.0, side.y, 1.0E-9);
        assertEquals(0.0, side.z, 1.0E-9);
    }
}
