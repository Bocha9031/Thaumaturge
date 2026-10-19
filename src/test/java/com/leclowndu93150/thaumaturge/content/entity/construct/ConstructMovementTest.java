package com.leclowndu93150.thaumaturge.content.entity.construct;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class ConstructMovementTest {
    @Test
    void selfMovementIsDampedHorizontallyButKeepsVerticalMotion() {
        Vec3 movement = new Vec3(10.0, 3.0, -6.0);

        Vec3 damped = EntityOwnedConstruct.dampHorizontalMovement(MoverType.SELF, movement, 5.0);

        assertEquals(new Vec3(2.0, 3.0, -1.2), damped);
    }

    @Test
    void externalMovementIsNotDamped() {
        Vec3 movement = new Vec3(10.0, 3.0, -6.0);

        Vec3 preserved = EntityOwnedConstruct.dampHorizontalMovement(MoverType.PISTON, movement, 5.0);

        assertEquals(movement, preserved);
    }
}
