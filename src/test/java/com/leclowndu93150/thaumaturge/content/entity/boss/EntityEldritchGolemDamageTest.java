package com.leclowndu93150.thaumaturge.content.entity.boss;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class EntityEldritchGolemDamageTest {
    @Test
    void overkillDamageTriggersTheHeadlessPhase() {
        assertTrue(EntityEldritchGolem.isFatalUnblockedHit(false, true, 21.0F, 20.0F, false, false));
        assertFalse(EntityEldritchGolem.isFatalUnblockedHit(false, true, 20.0F, 20.0F, false, false));
        assertFalse(EntityEldritchGolem.isFatalUnblockedHit(false, true, 19.0F, 20.0F, false, false));
    }

    @Test
    void headlessDeadBypassingAndInvulnerableHitsKeepTheirExistingDamagePath() {
        assertFalse(EntityEldritchGolem.isFatalUnblockedHit(true, true, 21.0F, 20.0F, false, false));
        assertFalse(EntityEldritchGolem.isFatalUnblockedHit(false, false, 21.0F, 20.0F, false, false));
        assertFalse(EntityEldritchGolem.isFatalUnblockedHit(false, true, 21.0F, 20.0F, true, false));
        assertFalse(EntityEldritchGolem.isFatalUnblockedHit(false, true, 21.0F, 20.0F, false, true));
    }
}
