package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EntityEldritchGuardianGoalTest {
    @Test
    void guardianUsesOrdinaryIdleStroll() {
        EntityEldritchGuardian guardian =
                new EntityEldritchGuardian(TTEntities.ELDRITCH_GUARDIAN.get(), Mockito.mock(ServerLevel.class));

        long strollGoals = guardian.goalSelector.getAvailableGoals().stream()
                .map(wrapped -> wrapped.getGoal())
                .filter(goal -> goal.getClass() == RandomStrollGoal.class)
                .count();

        assertEquals(1, strollGoals);
    }

    @Test
    void guardianFogSkipsSpectatorsAndAcceptsNearbyPlayers() {
        Player player = Mockito.mock(Player.class);
        Entity guardian = Mockito.mock(Entity.class);
        when(player.isAlive()).thenReturn(true);
        when(player.distanceToSqr(guardian)).thenReturn(1.0);

        when(player.isSpectator()).thenReturn(true);
        assertFalse(EntityEldritchGuardian.shouldReceiveFog(player, guardian, 16.0));

        when(player.isSpectator()).thenReturn(false);
        assertTrue(EntityEldritchGuardian.shouldReceiveFog(player, guardian, 16.0));
    }
}
