package com.leclowndu93150.thaumaturge.content.golem.ai;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.jspecify.annotations.Nullable;

public final class VolleyGoal extends Goal {
    private static final int STEADY_AIM_TICKS = 20;
    private static final float GAZE_TURN = 10.0F;
    private static final float GAZE_PITCH = 30.0F;
    private static final float WEAKEST_SHOT = 0.1F;

    private final Mob shooter;
    private final RangedAttackMob archer;
    private final double pace;
    private final int quickestReload;
    private final int slowestReload;
    private final float range;
    private int reload = -1;
    private int steadyTicks;

    public VolleyGoal(RangedAttackMob archer, double pace, int quickestReload, int slowestReload, float range) {
        this.archer = archer;
        this.shooter = (Mob) archer;
        this.pace = pace;
        this.quickestReload = quickestReload;
        this.slowestReload = slowestReload;
        this.range = range;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = liveTarget();
        return target != null && canShootAt(target);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = liveTarget();
        if (target == null) {
            return !shooter.getNavigation().isDone();
        }
        return canShootAt(target);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        steadyTicks = 0;
        reload = -1;
    }

    @Override
    public void start() {
        steadyTicks = 0;
        reload = -1;
    }

    @Override
    public void tick() {
        LivingEntity target = liveTarget();
        if (target == null) {
            return;
        }
        float reach = rangeFraction(target);
        boolean inSight = shooter.getSensing().hasLineOfSight(target);
        steadyTicks = inSight ? steadyTicks + 1 : 0;
        boolean inRange = reach <= 1.0F;
        if (inRange && steadyTicks >= STEADY_AIM_TICKS) {
            shooter.getNavigation().stop();
        } else {
            shooter.getNavigation().moveTo(target, pace);
        }
        shooter.getLookControl().setLookAt(target, GAZE_TURN, GAZE_PITCH);
        if (reload < 0) {
            reload = reloadFor(reach);
            return;
        }
        if (--reload > 0) {
            return;
        }
        if (!canShootAt(target)) {
            reload = -1;
            return;
        }
        archer.performRangedAttack(target, Mth.clamp(reach, WEAKEST_SHOT, 1.0F));
        reload = reloadFor(reach);
    }

    private int reloadFor(float reach) {
        return Mth.floor(reach * (slowestReload - quickestReload) + quickestReload);
    }

    private boolean canShootAt(LivingEntity target) {
        return rangeFraction(target) <= 1.0F && shooter.getSensing().hasLineOfSight(target);
    }

    private float rangeFraction(LivingEntity target) {
        return (float) (Math.sqrt(shooter.distanceToSqr(target)) / range);
    }

    private @Nullable LivingEntity liveTarget() {
        LivingEntity target = shooter.getTarget();
        return target != null && target.isAlive() ? target : null;
    }
}
