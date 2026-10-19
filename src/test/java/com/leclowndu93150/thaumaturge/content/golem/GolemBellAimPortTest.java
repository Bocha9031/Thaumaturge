package com.leclowndu93150.thaumaturge.content.golem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class GolemBellAimPortTest {
    @Test
    void samplesCoverTheIntendedTwentyFiveBlockReachAtTwoSamplesPerBlock() {
        Vec3 eye = new Vec3(1.5, 2.0, 3.5);
        Vec3 look = new Vec3(0.0, 0.0, 1.0);

        assertEquals(50, ItemGolemBell.AIM_SAMPLES);
        Vec3 first = ItemGolemBell.aimPoint(eye, look, 1);
        assertEquals(4.0, first.z, 1.0E-10);

        Vec3 last = ItemGolemBell.aimPoint(eye, look, ItemGolemBell.AIM_SAMPLES);
        assertEquals(28.5, last.z, 1.0E-10);
    }
}
