package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EntityEldritchGuardianGoalTest {
    @Test
    void guardianUsesWaterAvoidingIdleStroll() {
        EntityEldritchGuardian guardian = new EntityEldritchGuardian(
                TTEntities.ELDRITCH_GUARDIAN.get(), Mockito.mock(ServerLevel.class));

        long waterAvoidingGoals = guardian.goalSelector.getAvailableGoals().stream()
                .map(wrapped -> wrapped.getGoal())
                .filter(goal -> goal.getClass() == WaterAvoidingRandomStrollGoal.class)
                .count();

        assertEquals(1, waterAvoidingGoals);
    }
}
