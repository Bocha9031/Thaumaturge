package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityAspectOrb extends Entity {
    private static final EntityDataAccessor<String> DATA_ASPECT =
            SynchedEntityData.defineId(EntityAspectOrb.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_VALUE =
            SynchedEntityData.defineId(EntityAspectOrb.class, EntityDataSerializers.INT);

    public static final int MAX_AGE = 150;

    private static final String NO_ASPECT = "";
    private static final int DEFAULT_VALUE = 1;
    private static final float DEFAULT_HEALTH = 5.0F;
    private static final int TARGET_SEARCH_INTERVAL = 5;
    private static final double ATTRACT_RANGE = 8.0;
    private static final double ATTRACT_RANGE_SQR = ATTRACT_RANGE * ATTRACT_RANGE;
    private static final double ATTRACT_STRENGTH = 0.1;
    private static final double MIN_PULL_DISTANCE = 1.0E-4;
    private static final double GRAVITY = 0.02;
    private static final double HOP_SIDEWAYS = 0.1;
    private static final double HOP_UP_BASE = 0.05;
    private static final double HOP_UP_SPREAD = 0.15;
    private static final double AIR_DRAG = 0.98;
    private static final double BOUNCE_RETAINED = 0.7;
    private static final double MIN_BOUNCE_SPEED = 0.04;
    private static final double WATER_DRAG = 0.95;
    private static final double WATER_RISE = 0.002;
    private static final double WATER_MAX_RISE = 0.04;
    private static final double LAVA_TOSS_SIDEWAYS = 0.2;
    private static final double LAVA_TOSS_UP = 0.2;
    private static final float FIZZ_VOLUME = 0.4F;
    private static final float FIZZ_PITCH_BASE = 2.0F;
    private static final float FIZZ_PITCH_SPREAD = 0.4F;
    private static final int PICKUP_DELAY_TICKS = 2;
    private static final float CHIME_VOLUME = 0.08F;
    private static final float CHIME_PITCH_BASE = 1.7F;
    private static final float CHIME_PITCH_SPREAD = 0.3F;
    private static final float FULL_TURN_DEGREES = 360.0F;
    private static final int UNTINTED = 0xFFFFFF;

    private int age;
    private float health = DEFAULT_HEALTH;
    private @Nullable Player target;
    private String cachedColorAspect = NO_ASPECT;
    private int cachedColor = UNTINTED;

    public EntityAspectOrb(EntityType<? extends EntityAspectOrb> type, Level level) {
        super(type, level);
    }

    public EntityAspectOrb(Level level, double x, double y, double z, ResourceKey<IAspect> aspect, int value) {
        this(TTEntities.ASPECT_ORB.get(), level);
        setPos(x, y, z);
        setAspect(aspect);
        setValue(value);
        setYRot(level.getRandom().nextFloat() * FULL_TURN_DEGREES);
        setDeltaMovement(
                spread(level, HOP_SIDEWAYS),
                HOP_UP_BASE + level.getRandom().nextDouble() * HOP_UP_SPREAD,
                spread(level, HOP_SIDEWAYS));
    }

    private static double spread(Level level, double reach) {
        return (level.getRandom().nextDouble() - level.getRandom().nextDouble()) * reach;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ASPECT, NO_ASPECT);
        builder.define(DATA_VALUE, DEFAULT_VALUE);
    }

    public @Nullable ResourceKey<IAspect> getAspect() {
        String stored = entityData.get(DATA_ASPECT);
        if (stored.isEmpty()) {
            return null;
        }
        ResourceLocation id = stored.indexOf(':') >= 0
                ? ResourceLocation.tryParse(stored)
                : ResourceLocation.tryBuild(TTIds.MODID, stored);
        return id == null ? null : ResourceKey.create(IAspect.REGISTRY_KEY, id);
    }

    public void setAspect(ResourceKey<IAspect> aspect) {
        entityData.set(DATA_ASPECT, aspect.location().toString());
        cachedColorAspect = NO_ASPECT;
    }

    public int getValue() {
        return entityData.get(DATA_VALUE);
    }

    private void setValue(int value) {
        entityData.set(DATA_VALUE, Math.max(DEFAULT_VALUE, value));
    }

    public int getAge() {
        return age;
    }

    public int getAspectColor() {
        String rawAspect = entityData.get(DATA_ASPECT);
        if (!rawAspect.equals(cachedColorAspect)) {
            cachedColorAspect = rawAspect;
            ResourceKey<IAspect> key = getAspect();
            Holder<IAspect> holder = key == null ? null : Aspects.resolve(level(), key);
            cachedColor = holder == null ? UNTINTED : holder.value().color();
        }
        return cachedColor;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }

    @Override
    protected void doWaterSplashEffect() {}

    @Override
    public void tick() {
        super.tick();
        applyFluidsAndGravity();
        if (!level().noCollision(getBoundingBox())) {
            moveTowardsClosestSpace(getX(), (getBoundingBox().minY + getBoundingBox().maxY) * 0.5, getZ());
        }
        followTarget();
        double fallSpeed = getDeltaMovement().y;
        move(MoverType.SELF, getDeltaMovement());
        applyFriction(fallSpeed);
        age++;
        if (age >= MAX_AGE) {
            discard();
        }
    }

    private void applyFluidsAndGravity() {
        Vec3 movement = getDeltaMovement();
        if (isInLava()) {
            setDeltaMovement(spread(level(), LAVA_TOSS_SIDEWAYS), LAVA_TOSS_UP, spread(level(), LAVA_TOSS_SIDEWAYS));
            if (!level().isClientSide()) {
                playSound(
                        SoundEvents.GENERIC_EXTINGUISH_FIRE,
                        FIZZ_VOLUME,
                        FIZZ_PITCH_BASE + random.nextFloat() * FIZZ_PITCH_SPREAD);
            }
        } else if (isInWater()) {
            setDeltaMovement(
                    movement.x * WATER_DRAG,
                    Math.min(movement.y * WATER_DRAG + WATER_RISE, WATER_MAX_RISE),
                    movement.z * WATER_DRAG);
        } else {
            applyGravity();
        }
    }

    private void applyFriction(double fallSpeed) {
        double drag = AIR_DRAG;
        if (onGround()) {
            var below = getBlockPosBelowThatAffectsMyMovement();
            drag = level().getBlockState(below).getFriction(level(), below, this) * AIR_DRAG;
        }
        Vec3 slowed = getDeltaMovement().multiply(drag, AIR_DRAG, drag);
        if (verticalCollisionBelow && fallSpeed < -MIN_BOUNCE_SPEED) {
            slowed = new Vec3(slowed.x, -fallSpeed * BOUNCE_RETAINED, slowed.z);
        }
        setDeltaMovement(slowed);
    }

    private void followTarget() {
        ResourceKey<IAspect> aspect = getAspect();
        if (aspect == null) {
            target = null;
            return;
        }
        if (target != null && !canStillFollow(target)) {
            target = null;
        }
        if (target == null && tickCount % TARGET_SEARCH_INTERVAL == 0) {
            target = findTarget(aspect);
        }
        if (target != null) {
            pullToward(target);
        }
    }

    private boolean canStillFollow(Player player) {
        return !player.isRemoved()
                && player.isAlive()
                && !player.isSpectator()
                && player.distanceToSqr(this) <= ATTRACT_RANGE_SQR;
    }

    private @Nullable Player findTarget(ResourceKey<IAspect> aspect) {
        Player closest = null;
        double closestDistance = ATTRACT_RANGE_SQR;
        for (Player player : level().players()) {
            double distance = player.distanceToSqr(this);
            if (distance <= closestDistance && canStillFollow(player) && hasRoom(player, aspect)) {
                closest = player;
                closestDistance = distance;
            }
        }
        return closest;
    }

    private boolean hasRoom(Player player, ResourceKey<IAspect> aspect) {
        return !WandVisHelper.findWandInHotbarWithRoom(player, aspect, getValue())
                .isEmpty();
    }

    private void pullToward(Player player) {
        Vec3 toEyes = player.getEyePosition().subtract(position());
        double distance = toEyes.length();
        double closeness = 1.0 - distance / ATTRACT_RANGE;
        if (closeness <= 0.0 || distance < MIN_PULL_DISTANCE) {
            return;
        }
        setDeltaMovement(getDeltaMovement().add(toEyes.scale(closeness * closeness * ATTRACT_STRENGTH / distance)));
    }

    @Override
    public void playerTouch(Player player) {
        if (level().isClientSide() || player.takeXpDelay != 0 || isRemoved()) {
            return;
        }
        ResourceKey<IAspect> aspect = getAspect();
        if (aspect == null || !TTAspects.PRIMALS.contains(aspect)) {
            return;
        }
        int value = getValue();
        ItemStack wand = WandVisHelper.findWandInHotbarWithRoom(player, aspect, value);
        if (wand.isEmpty()) {
            return;
        }
        WandVisHelper.addVis(wand, aspect, value, true);
        player.takeXpDelay = PICKUP_DELAY_TICKS;
        player.take(this, 1);
        float pitch = CHIME_PITCH_BASE + random.nextFloat() * CHIME_PITCH_SPREAD;
        level().playSound(
                        null,
                        getX(),
                        getY(),
                        getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        getSoundSource(),
                        CHIME_VOLUME,
                        pitch);
        discard();
    }

    @Override
    public final boolean hurt(DamageSource source, float amount) {
        if (super.isInvulnerableTo(source)) {
            return false;
        }
        if (level().isClientSide()) {
            return true;
        }
        markHurt();
        health -= amount;
        if (health <= 0.0F) {
            discard();
        }
        return true;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag output) {
        output.putInt("Age", age);
        output.putString("Aspect", entityData.get(DATA_ASPECT));
        output.putFloat("Health", health);
        output.putInt("Value", getValue());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag input) {
        age = input.contains("Age", Tag.TAG_INT) ? input.getInt("Age") : input.getShort("Age");
        String savedAspect = input.getString("Aspect");
        entityData.set(DATA_ASPECT, input.contains("Aspect", Tag.TAG_STRING) ? savedAspect : NO_ASPECT);
        health = input.contains("Health", Tag.TAG_FLOAT)
                ? input.getFloat("Health")
                : input.contains("Health", Tag.TAG_SHORT) ? input.getShort("Health") : DEFAULT_HEALTH;
        setValue(input.contains("Value", Tag.TAG_INT) ? input.getInt("Value") : input.getShort("Value"));
        cachedColorAspect = NO_ASPECT;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }
}
