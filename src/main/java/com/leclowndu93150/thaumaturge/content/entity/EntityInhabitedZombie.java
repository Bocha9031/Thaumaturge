package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class EntityInhabitedZombie extends Zombie {
    private static final Map<EquipmentSlot, Float> NO_DROP_OVERRIDES = Map.of();
    private static final double MAX_HEALTH = 30.0;
    private static final double ATTACK_DAMAGE = 5.0;
    private static final double REINFORCEMENT_CHANCE = 0.0;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int CULTIST_PRIORITY = 3;
    private static final double SPAWN_EXCLUSION_HORIZONTAL = 32.0;
    private static final double SPAWN_EXCLUSION_VERTICAL = 16.0;
    private static final int DEATH_PARTICLES = 20;
    private static final double HALF = 0.5;
    private static final double DEATH_PARTICLE_SPEED = 0.02;
    private static final double DEATH_PARTICLE_HEIGHT_SPREAD = 0.25;

    public EntityInhabitedZombie(EntityType<? extends EntityInhabitedZombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, REINFORCEMENT_CHANCE);
    }

    public static boolean checkInhabitedSpawnRules(
            EntityType<EntityInhabitedZombie> type,
            ServerLevelAccessor level,
            MobSpawnType reason,
            BlockPos pos,
            RandomSource random) {
        AABB exclusion =
                new AABB(pos).inflate(SPAWN_EXCLUSION_HORIZONTAL, SPAWN_EXCLUSION_VERTICAL, SPAWN_EXCLUSION_HORIZONTAL);
        return level.getEntitiesOfClass(EntityInhabitedZombie.class, exclusion).isEmpty()
                && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        NearestAttackableTargetGoal<EntityCultist> huntCultists =
                new NearestAttackableTargetGoal<>(this, EntityCultist.class, true);
        HurtByTargetGoal retaliate = new HurtByTargetGoal(this).setAlertOthers();
        this.targetSelector.addGoal(CULTIST_PRIORITY, huntCultists);
        this.targetSelector.addGoal(RETALIATE_PRIORITY, retaliate);
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity killed) {
        return true;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {}

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData groupData) {

        this.equip(TTLootTables.EQUIPMENT_INHABITED_ZOMBIE, NO_DROP_OVERRIDES);

        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected void tickDeath() {
        if (this.level() instanceof ServerLevel server && !this.isRemoved()) {
            this.releaseCrab(server);
            this.puffOfSmoke(server);
            this.remove(RemovalReason.KILLED);
        }
    }

    protected void releaseCrab(ServerLevel level) {
        EntityEldritchCrab crab = this.createConversionCrab(level);
        if (crab == null) {
            return;
        }
        crab.moveTo(this.getX(), this.getEyeY(), this.getZ(), this.getYRot(), this.getXRot());
        crab.setYHeadRot(this.getYHeadRot());
        crab.setHelm(true);
        level.addFreshEntity(crab);
    }

    protected @Nullable EntityEldritchCrab createConversionCrab(ServerLevel level) {
        BlockPos spawnPos = this.blockPosition();
        return TTEntities.ELDRITCH_CRAB.get().create(level, null, spawnPos, MobSpawnType.CONVERSION, false, false);
    }

    protected void puffOfSmoke(ServerLevel level) {
        double halfWidth = this.getBbWidth() * HALF;
        double halfHeight = this.getBbHeight() * HALF;
        double heightSpread = this.getBbHeight() * DEATH_PARTICLE_HEIGHT_SPREAD;
        level.sendParticles(
                ParticleTypes.POOF,
                this.getX(),
                this.getY() + halfHeight,
                this.getZ(),
                DEATH_PARTICLES,
                halfWidth,
                heightSpread,
                halfWidth,
                DEATH_PARTICLE_SPEED);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.CRABTALK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }
}
