package com.leclowndu93150.thaumaturge.content.infusion.instability;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EjectEffectTest {
    @Test
    void thresholdConsumptionBeginsAtConfiguredStability() {
        EjectEffect effect = EjectEffect.of(false, EjectAftermath.FLUX).consumingAtOrBelow(-50.0F);

        assertFalse(effect.consumesAt(-49.0F));
        assertTrue(effect.consumesAt(-50.0F));
        assertTrue(effect.consumesAt(-75.0F));
    }

    @Test
    void unconditionalAndLegacyEjectionSettingsKeepTheirBehavior() {
        assertTrue(EjectEffect.of(true, EjectAftermath.FLUX).consumesAt(0.0F));
        assertFalse(EjectEffect.of(false, EjectAftermath.FLUX).consumesAt(-100.0F));
    }
}
