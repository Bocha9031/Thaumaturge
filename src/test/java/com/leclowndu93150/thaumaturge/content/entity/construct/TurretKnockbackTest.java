package com.leclowndu93150.thaumaturge.content.entity.construct;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class TurretKnockbackTest {
    @Test
    void turretTypesUseTheirConfiguredKnockbackShares() {
        assertEquals(2.0, EntityTurretCrossbow.knockbackPower(10.0, 0.2), 1.0E-9);
        assertEquals(0.5, EntityTurretCrossbow.knockbackPower(10.0, 0.05), 1.0E-9);
    }
}
