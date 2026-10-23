package com.leclowndu93150.thaumaturge.content.entity.cult;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultist.Aiming;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemOrb;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

public final class CultSpells {
    private static final float ORB_CHANCE = 0.34F;
    private static final float VOLLEY_CHANCE = 1.0F - ORB_CHANCE;
    private static final double LEAD_TICKS = 10.0;
    private static final float ORB_SPEED = 0.66F;
    private static final float ORB_INACCURACY = 3.0F;
    private static final float ORB_SOUND_VOLUME = 1.0F;
    private static final float ORB_SOUND_PITCH_SPREAD = 0.1F;
    private static final int VOLLEY_EVENT = 1009;
    private static final int VOLLEY_SIZE = 3;
    private static final float VOLLEY_SPREAD = 0.5F;
    private static final double VOLLEY_LIFT = 0.5;
    private static final List<WeightedAttack> ATTACKS = List.of(new WeightedAttack(ORB_CHANCE, CultSpells::orb), new WeightedAttack(VOLLEY_CHANCE, CultSpells::volley));

    private CultSpells() {}

    public static void cast(Mob caster, LivingEntity target, float distanceFactor) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return;
        }
        caster.swing(InteractionHand.MAIN_HAND);
        select(caster.getRandom()).fire(caster, level, target, distanceFactor);
    }

    private static Attack select(RandomSource random) {
        float roll = random.nextFloat();
        float threshold = 0.0F;
        for (WeightedAttack attack : ATTACKS) {
            threshold += attack.weight();
            if (roll < threshold) {
                return attack.attack();
            }
        }
        return ATTACKS.get(ATTACKS.size() - 1).attack();
    }

    private static void orb(Mob caster, ServerLevel level, LivingEntity target, float distanceFactor) {
        RandomSource random = caster.getRandom();
        Vec3 aim = Aiming.launchVector(caster.position(), target.position(), target.getKnownMovement(), LEAD_TICKS);
        EntityGolemOrb orb = new EntityGolemOrb(level, caster, target, true);
        orb.setPos(orb.getX() + aim.x, orb.getY() + aim.y, orb.getZ() + aim.z);
        orb.shoot(aim.x, aim.y, aim.z, ORB_SPEED, ORB_INACCURACY);
        level.addFreshEntity(orb);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), TTSounds.EGATTACK.get(), SoundSource.HOSTILE, ORB_SOUND_VOLUME, 1.0F + random.nextFloat() * ORB_SOUND_PITCH_SPREAD);
    }

    private static void volley(Mob caster, ServerLevel level, LivingEntity target, float distanceFactor) {
        RandomSource random = caster.getRandom();
        level.levelEvent(null, VOLLEY_EVENT, caster.blockPosition(), 0);
        double spread = VOLLEY_SPREAD * Math.sqrt(distanceFactor);
        double originY = caster.getY() + caster.getBbHeight() / 2.0;
        Vec3 origin = new Vec3(caster.getX(), originY, caster.getZ());
        Vec3 aimPoint = new Vec3(target.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0, target.getZ());
        for (int i = 0; i < VOLLEY_SIZE; i++) {
            double offsetX = random.nextGaussian() * spread;
            double offsetZ = random.nextGaussian() * spread;
            Vec3 direction = Aiming.launchVector(origin, aimPoint, Vec3.ZERO, 0.0, offsetX, offsetZ);
            SmallFireball fireball = new SmallFireball(level, caster, direction);
            fireball.setPos(fireball.getX(), originY + VOLLEY_LIFT, fireball.getZ());
            level.addFreshEntity(fireball);
        }
    }

    @FunctionalInterface
    private interface Attack {
        void fire(Mob caster, ServerLevel level, LivingEntity target, float distanceFactor);
    }

    private record WeightedAttack(float weight, Attack attack) {
    }
}
