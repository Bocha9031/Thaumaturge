package com.leclowndu93150.thaumaturge.content.entity.ranger;

import com.leclowndu93150.thaumaturge.content.entity.ai.LongRangeAttackGoal;
import com.leclowndu93150.thaumaturge.content.entity.cult.CultRally;
import com.leclowndu93150.thaumaturge.content.entity.cult.CultSpells;
import com.leclowndu93150.thaumaturge.content.warp.spawn.PortalSummons;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;

public class EntityCultistRangerCaptain extends AbstractCultistRanger implements RangedAttackMob {
    private static final String PORTAL_SUMMONED_KEY = "portal_summoned";
    private static final double MAX_HEALTH = 60.0;
    private static final int EXPERIENCE = 25;
    private static final double MOVE_SPEED = 1.0;
    private static final double MIN_SPELL_DISTANCE = 6.0;
    private static final int MIN_SPELL_INTERVAL = 30;
    private static final int MAX_SPELL_INTERVAL = 50;
    private static final float SPELL_RADIUS = 24.0F;
    private static final int SPELL_PRIORITY = 2;
    private static final int MELEE_PRIORITY = 3;

    private boolean portalSummoned;

    public EntityCultistRangerCaptain(EntityType<? extends EntityCultistRangerCaptain> type, Level level) {
        super(type, level);
        this.xpReward = EXPERIENCE;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createCultistAttributes(MAX_HEALTH);
    }

    @Override
    public boolean isPatrolLeader() {
        return true;
    }

    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(SPELL_PRIORITY, new LongRangeAttackGoal(this, MOVE_SPEED, MIN_SPELL_DISTANCE, MIN_SPELL_INTERVAL, MAX_SPELL_INTERVAL, SPELL_RADIUS));
        this.goalSelector.addGoal(MELEE_PRIORITY, new MeleeAttackGoal(this, MOVE_SPEED, false));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        CultSpells.cast(this, target, distanceFactor);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.tickCount % CultRally.INTERVAL == 0) {
            CultRally.rally(this, level);
        }
        if (!portalSummoned && this.getTarget() instanceof Player) {
            portalSummoned = PortalSummons.summonNear(level, this.blockPosition(), this.getRandom(), EntitySpawnReason.MOB_SUMMONED);
        }
    }

    @Override
    protected ResourceKey<LootTable> equipmentTable() {
        return TTLootTables.EQUIPMENT_CULTIST_RANGER_CAPTAIN;
    }

    @Override
    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor level, RandomSource random, DifficultyInstance difficulty) {
        this.enchantSpawnedWeapon(level, random, difficulty);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(PORTAL_SUMMONED_KEY, portalSummoned);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        portalSummoned = input.getBooleanOr(PORTAL_SUMMONED_KEY, false);
    }
}
