package com.leclowndu93150.thaumaturge.content.entity.projectile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

final class EntityGrapplePortTest {
    @Test
    void pullEasesFromSmallNearTugToStrongerFarTug() {
        Vec3 near = EntityGrapple.tugVector(new Vec3(1.0, 0.0, 0.0), Vec3.ZERO);
        Vec3 middle = EntityGrapple.tugVector(new Vec3(4.0, 0.0, 0.0), Vec3.ZERO);
        Vec3 far = EntityGrapple.tugVector(new Vec3(8.0, 0.0, 0.0), Vec3.ZERO);

        assertTrue(near.length() < middle.length());
        assertTrue(middle.length() < far.length());
        assertEquals(0.1, far.length(), 1.0E-9);
    }

    @Test
    void pullPointsFromTheOwnersEyesTowardTheAnchoredHook() {
        Vec3 eye = new Vec3(1.0, 2.0, 3.0);
        Vec3 hook = new Vec3(5.0, 5.0, 3.0);

        Vec3 tug = EntityGrapple.tugVector(hook, eye);

        assertTrue(tug.x > 0.0);
        assertTrue(tug.y > 0.0);
        assertEquals(0.0, tug.z, 1.0E-9);
        assertTrue(tug.length() > 0.0);
        assertTrue(tug.length() < 0.1);
        assertEquals(4.0 / 3.0, tug.x / tug.y, 1.0E-9);
    }

    @Test
    void coincidentHookAndOwnerHaveNoUndefinedPull() {
        assertEquals(Vec3.ZERO, EntityGrapple.tugVector(Vec3.ZERO, Vec3.ZERO));
    }
}
