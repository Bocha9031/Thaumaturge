package com.leclowndu93150.thaumaturge.content.spell.carrier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class SpellBatTargetingTest {
    @Test
    void enemyModeSkipsInvulnerablePlayersButAllyModeDoesNot() {
        assertFalse(SpellBat.allowsPlayerTarget(false, true));
        assertTrue(SpellBat.allowsPlayerTarget(false, false));
        assertTrue(SpellBat.allowsPlayerTarget(true, true));
    }

    @Test
    void batOnlyPursuesTargetsMatchingItsAllyMode() {
        assertTrue(SpellBat.matchesAllyMode(true, true));
        assertFalse(SpellBat.matchesAllyMode(true, false));
        assertFalse(SpellBat.matchesAllyMode(false, true));
        assertTrue(SpellBat.matchesAllyMode(false, false));
    }
}
