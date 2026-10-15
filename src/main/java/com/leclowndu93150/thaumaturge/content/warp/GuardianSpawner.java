package com.leclowndu93150.thaumaturge.content.warp;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchGuardian;
import com.leclowndu93150.thaumaturge.content.warp.spawn.SpawnSpots;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.event.EventHooks;

public final class GuardianSpawner {
    private static final int SPAWN_ATTEMPTS = 50;

    private GuardianSpawner() {}

    public static void spawn(ServerPlayer player, int count) {
        for (int i = 0; i < count; i++) {
            spawnGuardian(player);
        }
    }

    private static void spawnGuardian(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        RandomSource rand = player.getRandom();
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            BlockPos pos = SpawnSpots.ringCell(origin, rand);
            if (!level.getBlockState(pos.below()).isCollisionShapeFullBlock(level, pos.below())) {
                continue;
            }
            EntityEldritchGuardian guardian = TTEntities.ELDRITCH_GUARDIAN.get().create(level);
            if (guardian == null) {
                return;
            }
            guardian.moveTo(pos.getX(), pos.getY(), pos.getZ(), rand.nextFloat() * 360.0F, 0.0F);
            if (!canPlace(level, guardian)) {
                guardian.discard();
                continue;
            }
            if (level.addFreshEntity(guardian)) {
                guardian.setTarget(player);
            }
            return;
        }
    }

    public static void spawnPortal(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        RandomSource rand = player.getRandom();
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            BlockPos pos = SpawnSpots.ringCell(origin, rand);
            if (!level.getBlockState(pos.below()).isSolidRender(level, pos.below())) {
                continue;
            }
            EntityCultistPortalLesser portal =
                    TTEntities.CULTIST_PORTAL_LESSER.get().create(level);
            if (portal == null) {
                return;
            }
            portal.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0F, 0.0F);
            if (!canPlace(level, portal)) {
                portal.discard();
                continue;
            }
            EventHooks.finalizeMobSpawn(
                    portal, level, level.getCurrentDifficultyAt(portal.blockPosition()), MobSpawnType.EVENT, null);
            level.addFreshEntity(portal);
            WarpManager.sendActionBar(player, "warp.thaumaturge.text.16");
            return;
        }
    }

    private static boolean canPlace(ServerLevel level, Mob mob) {
        return level.noCollision(mob) && !level.containsAnyLiquid(mob.getBoundingBox());
    }
}
