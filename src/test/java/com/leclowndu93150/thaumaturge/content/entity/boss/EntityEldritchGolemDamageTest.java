package com.leclowndu93150.thaumaturge.content.entity.boss;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class EntityEldritchGolemDamageTest {
    @Test
    void lethalDamageTriggersTheHeadlessPhaseEvenWhenDamageExactlyMatchesHealth() {
        assertTrue(EntityEldritchGolem.isFatalUnblockedHit(false, 20.0F, 20.0F, false));
        assertTrue(EntityEldritchGolem.isFatalUnblockedHit(false, 21.0F, 20.0F, false));
        assertFalse(EntityEldritchGolem.isFatalUnblockedHit(false, 19.0F, 20.0F, false));
    }

    @Test
    void headlessAndInvulnerabilityBypassingHitsKeepTheirExistingDamagePath() {
        assertFalse(EntityEldritchGolem.isFatalUnblockedHit(true, 20.0F, 20.0F, false));
        assertFalse(EntityEldritchGolem.isFatalUnblockedHit(false, 20.0F, 20.0F, true));
    }
}
