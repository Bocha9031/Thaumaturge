package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.entity.ai.AltarFocusGoal;
import com.leclowndu93150.thaumaturge.content.entity.cult.CultSpells;
import com.leclowndu93150.thaumaturge.content.entity.ai.LongRangeAttackGoal;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

public class EntityCultistCleric extends EntityCultist implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> RITUALIST = SynchedEntityData.defineId(EntityCultistCleric.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<BlockPos> ANCHOR = SynchedEntityData.defineId(EntityCultistCleric.class, EntityDataSerializers.BLOCK_POS);
    private static final String RITUALIST_KEY = "ritualist";
    private static final double MAX_HEALTH = 24.0;
    private static final double MOVE_SPEED = 1.0;
    private static final double MIN_ATTACK_DISTANCE = 2.0;
    private static final int MIN_ATTACK_INTERVAL = 20;
    private static final int MAX_ATTACK_INTERVAL = 40;
    private static final float ATTACK_RADIUS = 24.0F;
    private static final int ALTAR_PRIORITY = 1;
    private static final int RANGED_PRIORITY = 2;
    private static final int MELEE_PRIORITY = 3;
    private static final int RAGE_LIMIT = 5;
    private static final int AMBIENT_INTERVAL = 500;
    private static final float ANCHOR_CENTER = 0.5F;
    private static final double ANCHOR_HEIGHT = 1.5;
    private static final float PITCH_STEP = 10.0F;
    private static final float HEAD_STEP = 40.0F;

    public int rage;

    private final RitualState ritual = new RitualState();

    public EntityCultistCleric(EntityType<? extends EntityCultistCleric> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createCultistAttributes(MAX_HEALTH);
    }

    @Override
    protected void registerGoals() {
        LongRangeAttackGoal ranged = new LongRangeAttackGoal(this, MOVE_SPEED, MIN_ATTACK_DISTANCE, MIN_ATTACK_INTERVAL, MAX_ATTACK_INTERVAL, ATTACK_RADIUS);
        this.goalSelector.addGoal(MELEE_PRIORITY, new MeleeAttackGoal(this, MOVE_SPEED, false));
        this.goalSelector.addGoal(RANGED_PRIORITY, ranged);
        this.goalSelector.addGoal(ALTAR_PRIORITY, new AltarFocusGoal(this));
        this.addIdleGoals();
        this.addTargetGoals();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANCHOR, BlockPos.ZERO);
        builder.define(RITUALIST, false);
    }

    public boolean isRitualist() {
        return this.ritual.isActive();
    }

    public void setRitualist(boolean ritualist) {
        this.ritual.set(ritualist);
    }

    public BlockPos ritualAnchor() {
        return this.ritual.anchor();
    }

    @Override
    protected ResourceKey<LootTable> equipmentTable() {
        return TTLootTables.EQUIPMENT_CULTIST_CLERIC;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        CultSpells.cast(this, target, distanceFactor);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        if (this.isRitualist()) {
            return false;
        }
        return super.removeWhenFarAway(distanceSqr);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean immune = this.isInvulnerableTo(level, source);
        if (!immune) {
            this.ritual.clear();
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        this.ritual.tick();
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        boolean stored = input.getBooleanOr(RITUALIST_KEY, false);
        super.readAdditionalSaveData(input);
        this.setRitualist(stored);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putBoolean(RITUALIST_KEY, this.isRitualist());
        super.addAdditionalSaveData(output);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.CHANT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return AMBIENT_INTERVAL;
    }

    private final class RitualState {
        boolean isActive() {
            return EntityCultistCleric.this.entityData.get(RITUALIST);
        }

        BlockPos anchor() {
            return EntityCultistCleric.this.entityData.get(ANCHOR);
        }

        void set(boolean ritualist) {
            EntityCultistCleric.this.entityData.set(RITUALIST, ritualist);
            if (!ritualist || !EntityCultistCleric.this.hasHome()) {
                return;
            }
            EntityCultistCleric.this.entityData.set(ANCHOR, EntityCultistCleric.this.getHomePosition());
        }

        void clear() {
            set(false);
        }

        void tick() {
            if (!isActive()) {
                return;
            }
            if (EntityCultistCleric.this.level().isClientSide()) {
                focusOnAnchor();
            } else if (EntityCultistCleric.this.rage >= RAGE_LIMIT) {
                clear();
            }
        }

        private void focusOnAnchor() {
            BlockPos anchor = anchor();
            Vec3 focus = new Vec3(anchor.getX() + ANCHOR_CENTER, anchor.getY() + ANCHOR_HEIGHT, anchor.getZ() + ANCHOR_CENTER);
            EntityCultistCleric.this.lookAt(focus, HEAD_STEP, PITCH_STEP);
        }
    }
}
