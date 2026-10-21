package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.api.taint.ITaintOrigin;
import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintFeeding;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractTaintSeed extends AbstractRootedTaint implements ITaintOrigin {
    private static final double SEED_FOLLOW_RANGE = 16.0;
    private static final float STRIKE_ANIMATION_START = 0.5F;
    private static final float STRIKE_ANIMATION_DECAY = 0.75F;
    private static final float STRIKE_ANIMATION_FLOOR = 0.001F;
    private static final int PULSE_INTERVAL = 20;
    private static final int FUME_COUNT = 3;
    private static final double FUME_RISE_BASE = 0.015;
    private static final double FUME_RISE_SPREAD = 0.015;
    private static final float FUME_SCALE = 1.5F;
    private static final float TICKS_PER_SECOND = 20.0F;
    private static final int SPREAD_HORIZONTAL_REACH = 3;
    private static final int AURA_REACH = 4;
    private static final int AURA_EFFECT_TICKS = 100;
    private static final String RESERVE_KEY = "taint_reserve";
    private static final String STARVE_KEY = "starve_ticks";

    public float attackAnim;

    private @Nullable BlockPos anchor;
    private float reserve;
    private int starveTicks;

    protected AbstractTaintSeed(EntityType<? extends AbstractTaintSeed> type, Level level) {
        super(type, level);
    }

    public abstract int getArea();

    public static AttributeSupplier.Builder createSeedAttributes(double maxHealth, double attackDamage) {
        return createRootedAttributes(maxHealth, attackDamage, SEED_FOLLOW_RANGE);
    }

    @Override
    protected void onStrike(ServerLevel level) {
        this.playSound(TTSounds.TENTACLE.get(), this.getSoundVolume(), this.getVoicePitch());
    }

    @Override
    protected void startStrikeAnimation() {
        this.attackAnim = STRIKE_ANIMATION_START;
    }

    @Override
    protected void tickStrikeAnimation() {
        this.attackAnim *= STRIKE_ANIMATION_DECAY;
        if (this.attackAnim < STRIKE_ANIMATION_FLOOR) {
            this.attackAnim = 0.0F;
        }
    }

    @Override
    protected void rootedServerStep(ServerLevel level) {
        BlockPos pos = this.blockPosition();
        if (!pos.equals(this.anchor)) {
            if (this.anchor != null) {
                TaintApi.removeTaintSeed(level, this.anchor);
            }
            this.anchor = pos;
            TaintApi.addTaintSeed(level, pos);
        }
        if (!this.isAlive()) {
            return;
        }
        if (this.tickCount % PULSE_INTERVAL != 0) {
            return;
        }
        emitFumes(level);
        TaintEcology.touchActiveSeed(level, pos);
        TaintBiomeManager.taintColumn(level, pos);
        TaintFeeding.feed(level, pos, this, PULSE_INTERVAL / TICKS_PER_SECOND);
        if (TaintFeeding.isDormant(this)) {
            starve(level);
        } else {
            this.starveTicks = 0;
            spreadFibres(level, pos);
        }
        if (!this.isAlive()) {
            return;
        }
        afflictNearby(level);
    }

    private void starve(ServerLevel level) {
        int limit = ThaumaturgeCommonConfig.TAINT_STARVE_TICKS.get();
        this.starveTicks += PULSE_INTERVAL;
        float damage = this.getMaxHealth() * PULSE_INTERVAL / limit;
        this.hurtServer(level, level.damageSources().starve(), this.starveTicks >= limit ? this.getMaxHealth() : damage);
    }

    @Override
    public boolean isLiveTaintOrigin() {
        return this.isAlive();
    }

    @Override
    public float getTaintReserve() {
        return this.reserve;
    }

    @Override
    public void setTaintReserve(float reserve) {
        this.reserve = Math.max(0.0F, reserve);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat(RESERVE_KEY, this.reserve);
        output.putInt(STARVE_KEY, this.starveTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.reserve = Math.max(0.0F, input.getFloatOr(RESERVE_KEY, 0.0F));
        this.starveTicks = input.getIntOr(STARVE_KEY, 0);
    }

    @Override
    public void onRemoval(RemovalReason reason) {
        super.onRemoval(reason);
        if (this.level() instanceof ServerLevel server && this.anchor != null) {
            TaintApi.removeTaintSeed(server, this.anchor);
            this.anchor = null;
        }
    }

    private void emitFumes(ServerLevel level) {
        RandomSource random = this.getRandom();
        for (int i = 0; i < FUME_COUNT; i++) {
            Vec3 at = new Vec3(this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth(), this.getY() + random.nextDouble() * this.getBbHeight(),
                    this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth());
            Effects.taint(level, at).motion(0.0, FUME_RISE_BASE + random.nextDouble() * FUME_RISE_SPREAD, 0.0).scale(FUME_SCALE).send();
        }
    }

    private void spreadFibres(ServerLevel level, BlockPos pos) {
        RandomSource random = this.getRandom();
        int area = this.getArea();
        int attempts = TaintFeeding.growthAttempts(this);
        int horizontal = SPREAD_HORIZONTAL_REACH * area;
        for (int i = 0; i < attempts; i++) {
            BlockPos target = pos.offset(offset(random, horizontal), offset(random, area), offset(random, horizontal));
            TaintHelper.growFromOrigin(level, target, this);
        }
    }

    private void afflictNearby(ServerLevel level) {
        int area = this.getArea();
        AABB reach = this.getBoundingBox().inflate(AURA_REACH * area);
        int amplifier = Math.max(area - 1, 0);
        for (LivingEntity creature : level.getEntitiesOfClass(LivingEntity.class, reach, this::canAfflict)) {
            creature.addEffect(new MobEffectInstance(TTMobEffects.FLUX_TAINT, AURA_EFFECT_TICKS, amplifier, true, false, false));
        }
    }

    private boolean canAfflict(LivingEntity creature) {
        return creature != this && creature.isAlive() && !creature.isSpectator() && !MobTraits.isTainted(creature);
    }

    private static int offset(RandomSource random, int reach) {
        return random.nextInt(reach * 2 + 1) - reach;
    }
}
