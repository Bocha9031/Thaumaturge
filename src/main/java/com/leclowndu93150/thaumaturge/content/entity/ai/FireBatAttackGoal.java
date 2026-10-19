package com.leclowndu93150.thaumaturge.content.entity.ai;

import com.leclowndu93150.thaumaturge.content.entity.EntityFireBat;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

public final class FireBatAttackGoal extends Goal {
    private static final double TARGET_HEIGHT_FACTOR = 2.0 / 3.0;
    private static final float MIN_ATTACK_REACH = 2.5F;
    private static final float REACH_WIDTH_FACTOR = 1.1F;
    private static final int COOLDOWN_BASE = 20;
    private static final int COOLDOWN_SPREAD = 20;
    private static final int EXPLODE_ONE_IN = 10;
    private static final float EXPLOSION_POWER = 1.5F;
    private static final float HURT_VOLUME = 0.1F;
    private static final float HURT_PITCH_BASE = 0.95F;
    private static final float HURT_PITCH_SPREAD = 0.1F;

    private final EntityFireBat bat;
    private int attackCooldown;

    public FireBatAttackGoal(EntityFireBat bat) {
        this.bat = bat;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return bat.getTarget() != null && !bat.isHanging();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = bat.getTarget();
        if (target == null) {
            return;
        }
        bat.getMoveControl()
                .setWantedPosition(
                        target.getX(),
                        target.getY() + target.getEyeHeight() * TARGET_HEIGHT_FACTOR,
                        target.getZ(),
                        1.0);
        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }
        float reach = Math.max(MIN_ATTACK_REACH, target.getBbWidth() * REACH_WIDTH_FACTOR);
        boolean verticalOverlap = target.getBoundingBox().maxY > bat.getBoundingBox().minY
                && target.getBoundingBox().minY < bat.getBoundingBox().maxY;
        if (!bat.hasLineOfSight(target)
                || bat.distanceTo(target) >= reach
                || !verticalOverlap
                || !(bat.level() instanceof ServerLevel server)) {
            return;
        }
        RandomSource random = bat.getRandom();
        attackCooldown = COOLDOWN_BASE + random.nextInt(COOLDOWN_SPREAD);
        if (random.nextInt(EXPLODE_ONE_IN) == 0) {
            detonate(server, target);
        } else {
            bite(server, target, random);
        }
    }

    private void bite(ServerLevel world, LivingEntity target, RandomSource random) {
        bat.doHurtTarget(target);
        float pitch = HURT_PITCH_BASE + (random.nextFloat() - random.nextFloat()) * HURT_PITCH_SPREAD;
        world.playSound(
                null,
                bat.getX(),
                bat.getY(),
                bat.getZ(),
                SoundEvents.BAT_HURT,
                bat.getSoundSource(),
                HURT_VOLUME,
                pitch);
    }

    private void detonate(ServerLevel world, LivingEntity target) {
        target.invulnerableTime = 0;
        world.explode(bat, bat.getX(), bat.getY(), bat.getZ(), EXPLOSION_POWER, false, Level.ExplosionInteraction.NONE);
        bat.discard();
    }
}
