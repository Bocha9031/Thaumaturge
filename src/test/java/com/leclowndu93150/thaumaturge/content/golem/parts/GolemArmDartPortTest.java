package com.leclowndu93150.thaumaturge.content.golem.parts;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemDart;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class GolemArmDartPortTest {
    private static final float POWER = 0.37F;

    private final GolemArmDart arms = new GolemArmDart();

    @Test
    void clientRangedCallbackDoesNotReadBodyOrSpawnDart() {
        IGolemAPI golem = mock(IGolemAPI.class);
        Level clientLevel = mock(Level.class);
        LivingEntity target = mock(LivingEntity.class);
        when(golem.level()).thenReturn(clientLevel);
        when(clientLevel.isClientSide()).thenReturn(true);

        try (MockedStatic<EntityGolemDart> darts = mockStatic(EntityGolemDart.class)) {
            arms.onRangedAttack(golem, target, POWER);

            verify(golem, never()).asEntity();
            darts.verifyNoInteractions();
        }
    }

    @Test
    void serverRangedCallbackUsesBodyRandomAndKeepsDoubleDamagePrecision() {
        IGolemAPI golem = mock(IGolemAPI.class);
        Level level = mock(Level.class);
        RandomSource levelRandom = mock(RandomSource.class);
        RandomSource bodyRandom = mock(RandomSource.class);
        LivingEntity body = mock(LivingEntity.class);
        LivingEntity target = mock(LivingEntity.class);
        when(golem.level()).thenReturn(level);
        when(golem.asEntity()).thenReturn(body);
        when(level.isClientSide()).thenReturn(false);
        when(level.getRandom()).thenReturn(levelRandom);
        when(body.getRandom()).thenReturn(bodyRandom);
        when(body.getAttributeValue(Attributes.ATTACK_DAMAGE)).thenReturn(10.1D);
        when(bodyRandom.nextGaussian()).thenReturn(0.5D);
        when(bodyRandom.nextFloat()).thenReturn(0.25F);
        clearInvocations(level);

        double expectedDamage = 10.1D / 3.0D + POWER + 0.5D * 0.25D;
        float expectedPitch = 1.0F / (0.25F * 0.4F + 0.8F);
        try (MockedStatic<EntityGolemDart> darts = mockStatic(EntityGolemDart.class)) {
            arms.onRangedAttack(golem, target, POWER);

            darts.verify(() -> EntityGolemDart.loose(
                    eq(body), eq(target), eq(expectedDamage), eq((double) (POWER * POWER)), eq(1.6F), eq(3.0F)));
        }
        verify(body).playSound(SoundEvents.ARROW_SHOOT, 1.0F, expectedPitch);
        verify(bodyRandom).nextGaussian();
        verify(bodyRandom).nextFloat();
        verify(level, never()).getRandom();
    }

    @Test
    void rangedGoalReturnsNullForNonMobImplementations() {
        RangedAttackMob archer = mock(RangedAttackMob.class);

        Goal goal = arms.createRangedGoal(archer);

        assertNull(goal);
    }

    @Test
    void rangedGoalIsCreatedForMobImplementations() {
        Mob shooter = mock(Mob.class, Mockito.withSettings().extraInterfaces(RangedAttackMob.class));
        RangedAttackMob archer = (RangedAttackMob) shooter;

        Goal goal = arms.createRangedGoal(archer);

        assertNotNull(goal);
    }
}
