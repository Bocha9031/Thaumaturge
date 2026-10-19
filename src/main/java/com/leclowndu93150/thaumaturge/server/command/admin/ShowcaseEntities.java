package com.leclowndu93150.thaumaturge.server.command.admin;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintacle;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSeed;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSeedPrime;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityCultistPortalGreater;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.AbstractHierophantSpell;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityHierophantCrescent;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityHierophantHammer;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityHierophantNova;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityHierophantSigil;
import com.leclowndu93150.thaumaturge.content.spell.carrier.SpellCloud;
import com.leclowndu93150.thaumaturge.content.spell.carrier.SpellProjectile;
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
    private static final double TAINT_SEED_Y_OFFSET = 1.0;
    private static final double TAINT_SEED_PRIME_Y_OFFSET = 3.0;

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
        if (entity instanceof AbstractHierophantSpell spell) {
            spell.setShowcaseAge(displayAge(spell));
        }
        double yOffset = displayYOffset(entity);
        if (yOffset != 0.0) {
            entity.setPos(entity.getX(), entity.getY() + yOffset, entity.getZ());
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
            if (entity instanceof EntityCultistPortalLesser portal) {
                portal.activeCounter = 50;
            }
            entity.tickCount = displayTickCount(entity);
            entity.setDeltaMovement(Vec3.ZERO);
            entity.setYHeadRot(entity.getYRot());
            entity.setYBodyRot(entity.getYRot());
            if (entity instanceof LivingEntity living) {
                living.yHeadRotO = living.yHeadRot;
                living.yBodyRotO = living.yBodyRot;
            }
            if (entity.level() == null || !entity.level().isClientSide() || !hasShowcaseParticleAnimation(entity)) {
                event.setCanceled(true);
            }
        }
    }

    static int displayTickCount(Entity entity) {
        if (entity instanceof AbstractTaintacle) {
            // The model animates its emergence from the ground based on entity age.
            return (int) Math.ceil(entity.getBbHeight() * 10.0F);
        }
        if (entity instanceof EntityCultistPortalGreater || entity instanceof EntityCultistPortalLesser) {
            // The portal renderer scales from zero over its first 50 ticks.
            return 50;
        }
        return 1;
    }

    static double displayYOffset(Entity entity) {
        if (entity instanceof EntityTaintSeedPrime) {
            return TAINT_SEED_PRIME_Y_OFFSET;
        }
        if (entity instanceof EntityTaintSeed) {
            return TAINT_SEED_Y_OFFSET;
        }
        return 0.0;
    }

    static int displayAge(AbstractHierophantSpell spell) {
        if (spell instanceof EntityHierophantSigil) {
            return EntityHierophantSigil.WARNING_TICKS + 2;
        }
        if (spell instanceof EntityHierophantCrescent) {
            return 2;
        }
        if (spell instanceof EntityHierophantHammer) {
            return 5;
        }
        if (spell instanceof EntityHierophantNova) {
            return 4;
        }
        return 1;
    }

    static boolean hasShowcaseParticleAnimation(Entity entity) {
        return entity instanceof SpellProjectile || entity instanceof SpellCloud;
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
