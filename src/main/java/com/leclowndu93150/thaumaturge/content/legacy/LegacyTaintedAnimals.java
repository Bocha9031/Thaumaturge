package com.leclowndu93150.thaumaturge.content.legacy;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class LegacyTaintedAnimals {
    private static final List<Replacement> REPLACEMENTS = List.of(
            new Replacement("taint_spider", TTEntities.LEGACY_TAINT_SPIDER, () -> EntityType.SPIDER, true),
            new Replacement("taint_creeper", TTEntities.LEGACY_TAINT_CREEPER, () -> EntityType.CREEPER, false),
            new Replacement("taint_cow", TTEntities.LEGACY_TAINT_COW, () -> EntityType.COW, false),
            new Replacement("taint_pig", TTEntities.LEGACY_TAINT_PIG, () -> EntityType.PIG, false),
            new Replacement("taint_chicken", TTEntities.LEGACY_TAINT_CHICKEN, () -> EntityType.CHICKEN, false),
            new Replacement("taint_sheep", TTEntities.LEGACY_TAINT_SHEEP, () -> EntityType.SHEEP, false),
            new Replacement("taint_villager", TTEntities.LEGACY_TAINT_VILLAGER, () -> EntityType.VILLAGER, false));

    private LegacyTaintedAnimals() {}

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Mob legacy) || !(entity.level() instanceof ServerLevel level) || legacy.isRemoved()) {
            return;
        }
        for (Replacement replacement : REPLACEMENTS) {
            if (legacy.getType() == replacement.legacy().get()) {
                if (replace(level, legacy, replacement)) {
                    event.setCanceled(true);
                }
                return;
            }
        }
    }

    private static boolean replace(ServerLevel level, Mob legacy, Replacement mapping) {
        Mob replacement = mapping.current().get().create(level);
        if (replacement == null) {
            Thaumaturge.LOGGER.error(
                    "Could not create {} to replace legacy {} at {}",
                    mapping.current().get(),
                    legacy.getType(),
                    legacy.blockPosition());
            return false;
        }
        List<Entity> riders = List.copyOf(legacy.getPassengers());
        Entity vehicle = legacy.getVehicle();
        float health = legacy.getHealth();
        replacement.restoreFrom(legacy);
        // Keep the legacy entity alive until NeoForge accepts the replacement.
        // A fresh identity lets both entities coexist during that attempt.
        replacement.setUUID(UUID.randomUUID());
        if (mapping.spiderling()) {
            replacement.setData(TTAttachments.LEGACY_TAINT_SPIDERLING, true);
        }
        if (!level.addFreshEntity(replacement)) {
            Thaumaturge.LOGGER.error(
                    "Could not add {} replacing legacy {} at {}",
                    mapping.current().get(),
                    legacy.getType(),
                    legacy.blockPosition());
            return false;
        }
        legacy.ejectPassengers();
        legacy.stopRiding();
        legacy.discard();
        MobTraits.add(replacement, TTMobTraits.TAINTED);
        replacement.setHealth(Math.min(health, replacement.getMaxHealth()));
        for (Entity rider : riders) {
            rider.startRiding(replacement);
        }
        if (vehicle != null) {
            replacement.startRiding(vehicle);
        }
        Thaumaturge.LOGGER.debug(
                "Replaced legacy {} with tainted {} at {}",
                legacy.getType(),
                mapping.current().get(),
                replacement.blockPosition());
        return true;
    }

    static List<ResourceLocation> legacyIds() {
        return REPLACEMENTS.stream()
                .map(replacement -> TTIds.rl(replacement.legacyPath()))
                .toList();
    }

    record Replacement(
            String legacyPath,
            Supplier<? extends EntityType<? extends Mob>> legacy,
            Supplier<EntityType<? extends Mob>> current,
            boolean spiderling) {}
}
