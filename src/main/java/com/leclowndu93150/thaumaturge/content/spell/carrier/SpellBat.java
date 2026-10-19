package com.leclowndu93150.thaumaturge.content.spell.carrier;

import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.entity.IBatAnimated;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jspecify.annotations.Nullable;

public final class SpellBat extends Monster implements TraceableEntity, IEntityWithComplexSpawn, IBatAnimated {
    private static final int LIFESPAN = 600;
    private static final int STRIKE_COOLDOWN = 40;
    private static final double TARGET_RANGE = 12.0;
    private static final double ROOST_ARRIVAL = 2.0;
    private static final int ROOST_SPAN = 6;
    private static final int ROOST_WIDTH = 2 * ROOST_SPAN + 1;
    private static final int ROOST_DEPTH_BELOW = 2;
    private static final int ROOST_HEIGHT = 6;
    private static final double STRIKE_REACH = 2.5;
    private static final double STRIKE_WIDTH_FACTOR = 1.1;
    private static final double CRUISE_HORIZONTAL = 0.5;
    private static final double CRUISE_VERTICAL = 0.7;
    private static final double VERTICAL_DAMPING = 0.4;
    private static final double STEER_RATE = 0.3;
    private static final float STRIKE_COST = 1.0F;
    private static final double AURA_SPREAD = 0.3;
    private static final double BODY_HALF = 0.5;
    private static final double MAX_HEALTH = 5.0;
    private static final double ATTACK_DAMAGE = 1.0;
    private static final float SOUND_VOLUME = 0.1F;
    private static final float STRIKE_VOLUME = 0.6F;
    private static final float STRIKE_PITCH_BASE = 1.4F;
    private static final float STRIKE_PITCH_RANGE = 0.3F;
    private static final float FORWARD_INPUT = 0.5F;
    private static final float QUARTER_TURN = 90.0F;
    private static final int ROOST_REROLL_ODDS = 30;
    private static final float DEGREES_PER_RADIAN = 180.0F / (float) Math.PI;

    public final AnimationState flyAnimationState = new AnimationState();

    private final CarrierCharge charge = new CarrierCharge();
    private @Nullable UUID owner;
    private boolean allies;
    private @Nullable BlockPos roost;
    private int strikeReadyAt;
    private @Nullable LivingEntity prey;

    public SpellBat(EntityType<? extends SpellBat> type, Level level) {
        super(type, level);
        xpReward = 0;
    }

    public static void release(
            ServerLevel level, LivingEntity owner, CarrierPayload payload, SpellTarget origin, boolean allies) {
        SpellBat bat = TTEntities.SPELL_BAT.get().create(level);
        if (bat == null) {
            return;
        }
        bat.charge.arm(payload);
        bat.owner = owner.getUUID();
        bat.allies = allies;
        bat.moveTo(origin.position().x, origin.position().y, origin.position().z, owner.getYRot(), 0.0F);
        level.addFreshEntity(bat);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
    }

    public int color() {
        return charge.look().color();
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        Entity resolved = owner == null
                ? null
                : level() instanceof ServerLevel server ? server.getEntity(owner) : level().getPlayerByUUID(owner);
        return resolved instanceof LivingEntity living ? living : null;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        charge.writeLook(buffer);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        charge.readLook(buffer);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag output) {
        HolderLookup.Provider registries = registryAccess();
        super.addAdditionalSaveData(output);
        charge.save(output, registries);
        if (owner != null) output.putUUID("owner", owner);
        output.putBoolean("allies", allies);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        HolderLookup.Provider registries = registryAccess();
        super.readAdditionalSaveData(input);
        charge.load(input, registries);
        owner = input.hasUUID("owner") ? input.getUUID("owner") : null;
        allies = (input.contains("allies") ? input.getBoolean("allies") : false);
    }

    @Override
    protected float getSoundVolume() {
        return SOUND_VOLUME;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override
    protected void doPush(Entity entity) {
        if (!allies) {
            super.doPush(entity);
        }
    }

    @Override
    public @Nullable PlayerTeam getTeam() {
        LivingEntity master = getOwner();
        return master != null ? master.getTeam() : super.getTeam();
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        LivingEntity master = getOwner();
        return master != null
                ? other == master || master.isAlliedTo(other) || other.isAlliedTo(master)
                : super.isAlliedTo(other);
    }

    @Override
    protected void checkFallDamage(double fall, boolean onGround, BlockState state, BlockPos pos) {}

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public void tick() {
        if (!level().isClientSide() && shouldExpire()) {
            discard();
            return;
        }
        super.tick();
        setDeltaMovement(getDeltaMovement().multiply(1.0, VERTICAL_DAMPING, 1.0));
        if (level().isClientSide()) {
            flyAnimationState.startIfStopped(tickCount);
            emitAura();
        }
    }

    private void emitAura() {
        Vec3 centre = getBoundingBox().getCenter();
        double spread = AURA_SPREAD + getBbWidth() * BODY_HALF;
        Vec3 at = centre.add(
                random.nextGaussian() * spread, random.nextGaussian() * spread, random.nextGaussian() * spread);
        CarrierPayload.particle(level(), charge.look(), at, Vec3.ZERO);
    }

    private boolean shouldExpire() {
        return level().getDifficulty() == Difficulty.PEACEFUL
                || tickCount > LIFESPAN
                || charge.isSpent()
                || getOwner() == null;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel) level();
        LivingEntity master = getOwner();
        if (master != null) {
            hunt(level, master);
        }
    }

    private void hunt(ServerLevel level, LivingEntity master) {
        if (prey != null && !valid(prey)) {
            prey = null;
        }
        if (prey == null) {
            prey = SpellTargeting.nearest(
                            level,
                            position(),
                            TARGET_RANGE,
                            candidate -> valid(candidate)
                                    && matchesAllyMode(allies, SpellTargeting.isAlly(master, candidate)))
                    .orElse(null);
        }
        if (prey == null) {
            fly(roostGoal(level));
        } else {
            pursue(level, prey);
        }
    }

    private void pursue(ServerLevel level, LivingEntity target) {
        Vec3 aim = target.getBoundingBox().getCenter();
        fly(aim);
        double reach = Math.max(STRIKE_REACH, STRIKE_WIDTH_FACTOR * target.getBbWidth());
        if (tickCount >= strikeReadyAt
                && getBoundingBox().getCenter().distanceTo(aim) <= reach
                && overlapsHeight(target)
                && hasLineOfSight(target)) {
            strike(level, target, aim);
        }
    }

    private boolean overlapsHeight(LivingEntity target) {
        return getBoundingBox().minY < target.getBoundingBox().maxY
                && target.getBoundingBox().minY < getBoundingBox().maxY;
    }

    private boolean valid(LivingEntity candidate) {
        if (candidate == this || candidate instanceof SpellBat || !candidate.isAlive()) {
            return false;
        }
        return !(candidate instanceof Player player) || allowsPlayerTarget(allies, player.getAbilities().invulnerable);
    }

    static boolean allowsPlayerTarget(boolean allies, boolean invulnerable) {
        return allies || !invulnerable;
    }

    static boolean matchesAllyMode(boolean allies, boolean candidateAlly) {
        return allies == candidateAlly;
    }

    private void strike(ServerLevel level, LivingEntity target, Vec3 aim) {
        strikeReadyAt = tickCount + STRIKE_COOLDOWN;
        Vec3 heading = aim.subtract(getBoundingBox().getCenter());
        charge.resume(level, List.of(SpellTarget.entity(target, heading)));
        float pitch = STRIKE_PITCH_BASE + random.nextFloat() * STRIKE_PITCH_RANGE;
        level.playSound(
                null, getX(), getY(), getZ(), SoundEvents.BAT_AMBIENT, SoundSource.HOSTILE, STRIKE_VOLUME, pitch);
        float healthLeft = getHealth() - STRIKE_COST;
        if (healthLeft > 0.0F) {
            setHealth(healthLeft);
        } else {
            kill();
        }
    }

    private Vec3 roostGoal(ServerLevel level) {
        if (roost != null && (!level.isEmptyBlock(roost) || roost.getY() <= level.getMinBuildHeight())) {
            roost = null;
        }
        if (roost == null
                || roost.closerToCenterThan(position(), ROOST_ARRIVAL)
                || random.nextInt(ROOST_REROLL_ODDS) == 0) {
            int dx = random.nextInt(ROOST_WIDTH) - ROOST_SPAN;
            int dy = random.nextInt(ROOST_HEIGHT) - ROOST_DEPTH_BELOW;
            int dz = random.nextInt(ROOST_WIDTH) - ROOST_SPAN;
            roost = BlockPos.containing(getX() + dx, getY() + dy, getZ() + dz);
        }
        return Vec3.atCenterOf(roost);
    }

    private void fly(Vec3 goal) {
        Vec3 motion = getDeltaMovement();
        double pullX = (Math.signum(goal.x - getX()) * CRUISE_HORIZONTAL - motion.x) * STEER_RATE;
        double pullY = (Math.signum(goal.y - getY()) * CRUISE_VERTICAL - motion.y) * STEER_RATE;
        double pullZ = (Math.signum(goal.z - getZ()) * CRUISE_HORIZONTAL - motion.z) * STEER_RATE;
        Vec3 next = motion.add(pullX, pullY, pullZ);
        setDeltaMovement(next);
        float yaw = (float) Mth.atan2(next.z, next.x) * DEGREES_PER_RADIAN - QUARTER_TURN;
        setYRot(yaw);
        setYHeadRot(yaw);
        yBodyRot = yaw;
        zza = FORWARD_INPUT;
    }

    private final AnimationState restAnimationState = new AnimationState();

    @Override
    public AnimationState flyAnimation() {
        return flyAnimationState;
    }

    @Override
    public AnimationState restAnimation() {
        return restAnimationState;
    }

    @Override
    public boolean isResting() {
        return false;
    }
}
