package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;

class EquipmentCombatTest {
    @Test
    void playerCombatUsesVanillaFriendlyFireRules() {
        Player attacker = mock(Player.class);
        Player target = mock(Player.class);
        when(attacker.canHarmPlayer(target)).thenReturn(false);

        assertFalse(EquipmentCombat.canAffect(attacker, target));
        verify(attacker).canHarmPlayer(target);
    }

    @Test
    void allowedPlayerCombatAndNonPlayerTargetsRemainEligible() {
        Player attacker = mock(Player.class);
        Player target = mock(Player.class);
        when(attacker.canHarmPlayer(target)).thenReturn(true);

        assertTrue(EquipmentCombat.canAffect(attacker, target));
        assertTrue(EquipmentCombat.canAffect(attacker, mock(LivingEntity.class)));
    }
}
