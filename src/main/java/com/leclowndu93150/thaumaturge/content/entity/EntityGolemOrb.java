package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityGolemOrb extends ThrowableProjectile implements ISidedHurt {
    private static final EntityDataAccessor<Boolean> DATA_RED =
            SynchedEntityData.defineId(EntityGolemOrb.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TARGET =
            SynchedEntityData.defineId(EntityGolemOrb.class, EntityDataSerializers.INT);
    private static final int RED_LIFE_TICKS = 240;
    private static final int WHITE_LIFE_TICKS = 160;
    private static final float RED_DAMAGE_FACTOR = 1.0F;
    private static final float WHITE_DAMAGE_FACTOR = 0.6F;
    private static final float PICK_RADIUS = 0.5F;
    private static final double HOMING_ACCEL = 0.2;
    private static final float HOMING_CLAMP = 0.25F;
    private static final float EYE_OFFSET = 0.1F;
    private static final int NO_TARGET = -1;

    public EntityGolemOrb(EntityType<? extends EntityGolemOrb> type, Level level) {
        super(type, level);
    }

    public EntityGolemOrb(Level level, LivingEntity shooter, @Nullable LivingEntity target, boolean red) {
        super(TTEntities.GOLEM_ORB.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - EYE_OFFSET, shooter.getZ());
        this.entityData.set(DATA_RED, red);
        this.entityData.set(DATA_TARGET, target == null ? NO_TARGET : target.getId());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_RED, false);
        entityData.define(DATA_TARGET, NO_TARGET);
    }

    public boolean isRed() {
        return this.entityData.get(DATA_RED);
    }

    private int lifetime() {
        return this.isRed() ? RED_LIFE_TICKS : WHITE_LIFE_TICKS;
    }

    private float damageFactor() {
        return this.isRed() ? RED_DAMAGE_FACTOR : WHITE_DAMAGE_FACTOR;
    }

    private @Nullable LivingEntity target() {
        int id = this.entityData.get(DATA_TARGET);
        if (id == NO_TARGET) {
            return null;
        }
        return this.level().getEntity(id) instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide() && this.tickCount > this.lifetime()) {
            this.discard();
            return;
        }
        super.tick();
        LivingEntity target = this.isRemoved() ? null : this.target();
        if (target != null) {
            double distSq = this.distanceToSqr(target);
            if (distSq > 0.0) {
                double dx = (target.getX() - this.getX()) / distSq;
                double dy = (target.getY() + target.getBbHeight() * 0.6 - this.getY()) / distSq;
                double dz = (target.getZ() - this.getZ()) / distSq;
                Vec3 movement = this.getDeltaMovement().add(dx * HOMING_ACCEL, dy * HOMING_ACCEL, dz * HOMING_ACCEL);
                this.setDeltaMovement(
                        Mth.clamp((float) movement.x, -HOMING_CLAMP, HOMING_CLAMP),
                        Mth.clamp((float) movement.y, -HOMING_CLAMP, HOMING_CLAMP),
                        Mth.clamp((float) movement.z, -HOMING_CLAMP, HOMING_CLAMP));
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof EntityGolemOrb) && super.canHitEntity(entity);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return PICK_RADIUS;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel server && !this.isRemoved()) {
            this.impact(server);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level() instanceof ServerLevel server
                && this.getOwner() instanceof LivingEntity caster
                && caster.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)) {
            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * this.damageFactor();
            result.getEntity().hurt(this.damageSources().indirectMagic(this, caster), damage);
        }
    }

    private void impact(ServerLevel level) {
        this.playVaried(level, TTSounds.SHOCK.get());
        this.discard();
    }

    private void playVaried(ServerLevel level, SoundEvent sound) {
        if (this.isSilent()) {
            return;
        }
        float pitch = 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
        level.playSound(null, this.getX(), this.getY(), this.getZ(), sound, this.getSoundSource(), 1.0F, pitch);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return hurtSided(level(), source, amount);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (this.isInvulnerableTo(source) || attacker == null) {
            return false;
        }
        this.setDeltaMovement(attacker.getLookAngle().scale(0.9));
        this.markHurt();
        this.playVaried(level, TTSounds.ZAP.get());
        return true;
    }

    @Override
    public boolean hurtClient(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
