package com.leclowndu93150.thaumaturge.content.equipment.shield;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.phys.Vec3;

final class ShieldReflection {
    private static final float HALF_TURN = 180.0F;
    private static final int REFLECT_SHIELD_DAMAGE = 3;
    private static final ProjectileDeflection MIRROR = (projectile, deflector, random) -> {
        projectile.setDeltaMovement(projectile.getDeltaMovement().reverse());
        projectile.setYRot(projectile.getYRot() + HALF_TURN);
        projectile.yRotO += HALF_TURN;
        projectile.needsSync = true;
    };

    private ShieldReflection() {}

    static boolean facing(LivingEntity blocker, Vec3 target) {
        Vec3 view = blocker.calculateViewVector(0.0F, blocker.getYHeadRot());
        Vec3 toward = target.subtract(blocker.position());
        return new Vec3(toward.x, 0.0, toward.z).dot(view) > 0.0;
    }

    static void reflect(Projectile projectile, LivingEntity blocker, ItemStack shield) {
        projectile.deflect(MIRROR, blocker, EntityReference.<Entity>of(blocker), false);
        BlocksAttacks blocks = shield.get(DataComponents.BLOCKS_ATTACKS);
        if (blocks != null && blocker.level() instanceof ServerLevel level) {
            blocks.onBlocked(level, blocker);
            blocks.hurtBlockingItem(level, shield, blocker, blocker.getUsedItemHand(), 0.0F, REFLECT_SHIELD_DAMAGE);
        }
    }
}
