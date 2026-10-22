package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSwarm;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class EntityTaintSporeSwarmer extends AbstractTaintSpore {
    private static final double MAX_HEALTH = 75.0;
    private static final int RELEASE_INTERVAL = 500;
    private static final double PLAYER_RANGE = 16.0;
    private static final double SWARM_SPACING = 16.0;
    private static final double RELEASE_HEIGHT = 0.5;

    public EntityTaintSporeSwarmer(EntityType<? extends EntityTaintSporeSwarmer> type, Level level) {
        super(type, level);
        setSporeSize(MAX_SIZE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createSporeAttributes(MAX_HEALTH);
    }

    @Override
    protected boolean requiresStalkSupport() {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel server && tickCount % RELEASE_INTERVAL == 0 && !isRemoved() && mayRelease(server)) {
            signalRelease(server);
            releaseSwarm(server, RELEASE_HEIGHT);
        }
    }

    private boolean mayRelease(ServerLevel level) {
        return level.getDifficulty() != Difficulty.PEACEFUL && TaintBiomeManager.isTainted(level, blockPosition()) && level.hasNearbyAlivePlayer(getX(), getY(), getZ(), PLAYER_RANGE)
                && level.getEntitiesOfClass(EntityTaintSwarm.class, new AABB(blockPosition()).inflate(SWARM_SPACING)).isEmpty();
    }

    private void releaseSwarm(ServerLevel level, double height) {
        EntityTaintSwarm.release(level, getX(), getY() + height, getZ());
    }

    @Override
    protected void onBurst(ServerLevel level) {
        if (level.getDifficulty() != Difficulty.PEACEFUL) {
            releaseSwarm(level, 0.0);
        }
    }
}
