package com.leclowndu93150.thaumaturge.content.entity.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

final class WispZapGoalPortTest {
    @Test
    void classifiesStationaryTargetsUsingEveryMotionAxis() {
        assertTrue(WispZapGoal.isStill(new Vec3(0.1, -0.1, 0.0)));
        assertFalse(WispZapGoal.isStill(new Vec3(0.0, 0.1001, 0.0)));
    }

    @Test
    void stationaryTargetsHaveTheHigherHitChance() {
        assertEquals(0.66F, WispZapGoal.hitChance(true));
        assertEquals(0.4F, WispZapGoal.hitChance(false));
    }

    @Test
    void unavailableTargetsDecayPositiveChargeWithoutChangingRechargeDelay() {
        assertEquals(19, WispZapGoal.decayCharge(20));
        assertEquals(0, WispZapGoal.decayCharge(0));
        assertEquals(-8, WispZapGoal.decayCharge(-8));
    }
}
