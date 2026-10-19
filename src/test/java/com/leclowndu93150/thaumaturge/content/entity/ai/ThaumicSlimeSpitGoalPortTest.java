package com.leclowndu93150.thaumaturge.content.entity.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

final class ThaumicSlimeSpitGoalPortTest {
    @Test
    void spitStartsOneBlockAheadInTheHorizontalTargetDirection() {
        Vec3 origin = new Vec3(1.0, 2.0, 3.0);
        Vec3 target = new Vec3(4.0, 40.0, 7.0);

        assertEquals(new Vec3(1.6, 2.0, 3.8), ThaumicSlimeSpitGoal.launchStart(origin, target));
    }

    @Test
    void spitAimLeadsUpwardForDistanceAndTargetHeight() {
        Vec3 start = new Vec3(1.0, 2.0, 3.0);
        Vec3 target = new Vec3(4.0, 5.0, 6.0);

        assertEquals(new Vec3(4.0, 8.0, 6.0), ThaumicSlimeSpitGoal.aimPoint(start, target, 3.0, 10.0));
    }
}
