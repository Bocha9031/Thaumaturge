package com.leclowndu93150.thaumaturge.server.command.admin;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintacle;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityCultistPortalGreater;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Keeps command-created specimens inert on both sides, including entities with custom lifetimes. */
@EventBusSubscriber(modid = TTIds.MODID)
public final class ShowcaseEntities {
    private static final String MARKER = "thaumaturge_showcase";

    private ShowcaseEntities() {}

    static void freeze(Entity entity) {
        entity.addTag(MARKER);
        // The name style also reaches clients through vanilla entity data and survives world saves.
        entity.setCustomName(entity.getType().getDescription().copy().withStyle(style -> style.withInsertion(MARKER)));
        entity.setCustomNameVisible(true);
        entity.setNoGravity(true);
        entity.setInvulnerable(true);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setYHeadRot(entity.getYRot());
        entity.setYBodyRot(entity.getYRot());
        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
            mob.setPersistenceRequired();
        }
        if (entity instanceof EntityCultistPortalLesser portal) {
            portal.setActive(true);
            portal.activeCounter = 50;
        }
        if (entity instanceof LivingEntity living) {
            living.setHealth(living.getMaxHealth());
        }
        if (entity instanceof ItemEntity item) {
            item.setItem(new ItemStack(TTItems.THAUMONOMICON.get()));
            item.setPickUpDelay(32767);
            item.setUnlimitedLifetime();
        }
    }

    public static boolean isDisplay(Entity entity) {
        Component name = entity.getCustomName();
        return entity.getTags().contains(MARKER)
                || name != null && MARKER.equals(name.getStyle().getInsertion());
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        if (isDisplay(entity)) {
            if (entity instanceof EntityFluxRift rift && rift.outline.isEmpty()) {
                rift.resize(rift.currentSize());
            }
            entity.tickCount = displayTickCount(entity);
            entity.setDeltaMovement(Vec3.ZERO);
            entity.setYHeadRot(entity.getYRot());
            entity.setYBodyRot(entity.getYRot());
            if (entity instanceof LivingEntity living) {
                living.yHeadRotO = living.yHeadRot;
                living.yBodyRotO = living.yBodyRot;
            }
            event.setCanceled(true);
        }
    }

    static int displayTickCount(Entity entity) {
        if (entity instanceof AbstractTaintacle) {
            // The model animates its emergence from the ground based on entity age.
            return (int) Math.ceil(entity.getBbHeight() * 10.0F);
        }
        if (entity instanceof EntityCultistPortalGreater) {
            // The portal renderer scales from zero over its first 50 ticks.
            return 50;
        }
        return 1;
    }

    @SubscribeEvent
    public static void onInvulnerability(EntityInvulnerabilityCheckEvent event) {
        if (isDisplay(event.getEntity())) {
            event.setInvulnerable(true);
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (isDisplay(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onDespawn(MobDespawnEvent event) {
        if (isDisplay(event.getEntity())) {
            event.setResult(MobDespawnEvent.Result.DENY);
        }
    }
}
