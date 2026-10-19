package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

final class EntityGolemDartPortTest {
    @Test
    void aimIsCalculatedFromTheProjectileOrigin() {
        Vec3 projectile = new Vec3(2.0, 3.0, 4.0);
        Vec3 targetEye = new Vec3(7.0, 9.0, 11.0);

        assertEquals(new Vec3(5.0, 8.0, 7.0), EntityGolemDart.aimVector(projectile, targetEye, 2.0));
    }
}
