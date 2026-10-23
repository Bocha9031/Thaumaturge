package com.leclowndu93150.thaumaturge.content.warp.spawn;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.neoforged.neoforge.event.EventHooks;

public final class PortalSummons {
    private static final double CELL_CENTER = 0.5;
    private static final double FLOOR_LIFT = 1.0;

    private PortalSummons() {}

    public static boolean summon(ServerLevel level, ServerPlayer player, RandomSource random) {
        return summonNear(level, player.blockPosition(), random, EntitySpawnReason.EVENT);
    }

    public static boolean summonNear(ServerLevel level, BlockPos origin, RandomSource random, EntitySpawnReason reason) {
        EntityCultistPortalLesser portal = TTEntities.CULTIST_PORTAL_LESSER.get().create(level, reason);
        if (portal == null) {
            return false;
        }
        for (int attempt = 0; attempt < SpawnSpots.PLACEMENT_ATTEMPTS; attempt++) {
            BlockPos cell = SpawnSpots.ringCell(origin, random);
            if (!SpawnSpots.standsOnSolidRender(level, cell)) {
                continue;
            }
            portal.snapTo(cell.getX() + CELL_CENTER, cell.getY() + FLOOR_LIFT, cell.getZ() + CELL_CENTER, 0.0F, 0.0F);
            if (!SpawnSpots.hasFreeSpace(level, portal)) {
                continue;
            }
            EventHooks.finalizeMobSpawn(portal, level, level.getCurrentDifficultyAt(cell.above()), reason, null);
            level.addFreshEntity(portal);
            return true;
        }
        return false;
    }
}
