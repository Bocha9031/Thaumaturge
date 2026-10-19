package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
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
}
