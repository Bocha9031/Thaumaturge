package com.leclowndu93150.thaumaturge.content.golem.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class VolleyGoalPortTest {
    private static final double MOVE_SPEED = 1.0D;
    private static final int MIN_INTERVAL = 20;
    private static final int MAX_INTERVAL = 25;
    private static final float RANGE = 16.0F;

    @Test
    void deadTargetsCannotStartContinueOrAdvanceTheGoal() {
        GoalFixture fixture = new GoalFixture(0.0D);
        when(fixture.target.isAlive()).thenReturn(false);
        when(fixture.navigation.isDone()).thenReturn(true);

        assertFalse(fixture.goal.canUse());
        assertFalse(fixture.goal.canContinueToUse());
        clearInvocations(fixture.shooter, fixture.navigation, fixture.sensing, fixture.lookControl);

        fixture.goal.tick();

        verify(fixture.shooter, never()).getSensing();
        verify(fixture.shooter, never()).getLookControl();
        verify(fixture.navigation, never()).moveTo(fixture.target, MOVE_SPEED);
        verify(fixture.navigation, never()).stop();
        verify(fixture.archer, never()).performRangedAttack(fixture.target, 0.1F);
    }

    @Test
    void rangedGoalYieldsToMeleeWhenTargetIsOutOfRangeOrObscured() {
        GoalFixture fixture = new GoalFixture((RANGE + 1.0D) * (RANGE + 1.0D));

        assertFalse(fixture.goal.canUse());
        assertFalse(fixture.goal.canContinueToUse());

        when(fixture.shooter.distanceToSqr(fixture.target)).thenReturn(0.0D);
        when(fixture.sensing.hasLineOfSight(fixture.target)).thenReturn(false);

        assertFalse(fixture.goal.canUse());
        assertFalse(fixture.goal.canContinueToUse());
    }

    @Test
    void rangedGoalStartsAndContinuesWhenTargetIsShootable() {
        GoalFixture fixture = new GoalFixture(0.0D);

        assertTrue(fixture.goal.canUse());
        assertTrue(fixture.goal.canContinueToUse());
    }

    @Test
    void startResetsTheShotCountdownAndFiresOnTheConfiguredMinimumCadence() {
        GoalFixture fixture = new GoalFixture(0.0D);
        fixture.goal.start();
        fixture.goal.tick();
        for (int tick = 0; tick < 5; tick++) {
            fixture.goal.tick();
        }

        fixture.goal.start();
        fixture.goal.tick();
        for (int tick = 0; tick < MIN_INTERVAL - 1; tick++) {
            fixture.goal.tick();
        }
        verify(fixture.archer, never()).performRangedAttack(fixture.target, 0.1F);

        fixture.goal.tick();

        verify(fixture.archer, times(1)).performRangedAttack(fixture.target, 0.1F);
    }

    @Test
    void blockedShotRestartsTheCadenceAfterSightReturns() {
        GoalFixture fixture = new GoalFixture(0.0D);
        AtomicBoolean visible = new AtomicBoolean(false);
        when(fixture.sensing.hasLineOfSight(fixture.target))
                .thenAnswer((org.mockito.invocation.InvocationOnMock invocation) -> visible.get());
        fixture.goal.start();
        fixture.goal.tick();
        for (int tick = 0; tick < MIN_INTERVAL; tick++) {
            fixture.goal.tick();
        }
        verify(fixture.archer, never()).performRangedAttack(fixture.target, 0.1F);

        visible.set(true);
        fixture.goal.tick();
        for (int tick = 0; tick < MIN_INTERVAL - 1; tick++) {
            fixture.goal.tick();
        }
        verify(fixture.archer, never()).performRangedAttack(fixture.target, 0.1F);

        fixture.goal.tick();

        verify(fixture.archer, times(1)).performRangedAttack(fixture.target, 0.1F);
    }

    @Test
    void configuredMaximumRangeUsesTheMaximumShotInterval() {
        GoalFixture fixture = new GoalFixture(RANGE * RANGE);
        fixture.goal.start();
        fixture.goal.tick();
        for (int tick = 0; tick < MAX_INTERVAL - 1; tick++) {
            fixture.goal.tick();
        }
        verify(fixture.archer, never()).performRangedAttack(fixture.target, 1.0F);

        fixture.goal.tick();

        verify(fixture.archer, times(1)).performRangedAttack(fixture.target, 1.0F);
        assertTrue(fixture.goal.canContinueToUse());
    }

    private static final class GoalFixture {
        private final Mob shooter;
        private final RangedAttackMob archer;
        private final LivingEntity target;
        private final PathNavigation navigation;
        private final Sensing sensing;
        private final LookControl lookControl;
        private final VolleyGoal goal;

        private GoalFixture(double distanceSqr) {
            shooter = mock(Mob.class, Mockito.withSettings().extraInterfaces(RangedAttackMob.class));
            archer = (RangedAttackMob) shooter;
            target = mock(LivingEntity.class);
            navigation = mock(PathNavigation.class);
            sensing = mock(Sensing.class);
            lookControl = mock(LookControl.class);
            when(shooter.getTarget()).thenReturn(target);
            when(target.isAlive()).thenReturn(true);
            when(shooter.distanceToSqr(target)).thenReturn(distanceSqr);
            when(shooter.getNavigation()).thenReturn(navigation);
            when(shooter.getSensing()).thenReturn(sensing);
            when(shooter.getLookControl()).thenReturn(lookControl);
            when(navigation.isDone()).thenReturn(true);
            when(sensing.hasLineOfSight(target)).thenReturn(true);
            goal = new VolleyGoal(archer, MOVE_SPEED, MIN_INTERVAL, MAX_INTERVAL, RANGE);
        }
    }
}
