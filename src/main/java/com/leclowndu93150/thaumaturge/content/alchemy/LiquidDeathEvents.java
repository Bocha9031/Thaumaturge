package com.leclowndu93150.thaumaturge.content.alchemy;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.damagesource.TTDamageTypes;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class LiquidDeathEvents {
    private static final int POINTS_PER_EXTRA_CRYSTAL = 10;

    private LiquidDeathEvents() {}

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level) || !event.getSource().is(TTDamageTypes.DISSOLVE)) {
            return;
        }

        AspectList aspects = EntityAspects.of(entity);
        if (aspects.isEmpty()) {
            return;
        }

        RandomSource random = entity.getRandom();
        int crystalCount = rollCrystalCount(aspects.totalAmount(), random);
        for (int i = 0; i < crystalCount; i++) {
            var aspect = aspects.entries().get(random.nextInt(aspects.size())).aspect();
            ItemEntity drop = new ItemEntity(
                    level, entity.getX(), entity.getEyeY(), entity.getZ(), EssentiaCrystalFactory.of(aspect));
            drop.setDefaultPickUpDelay();
            event.getDrops().add(drop);
        }
    }

    static int rollCrystalCount(int totalAspectPoints, RandomSource random) {
        if (totalAspectPoints <= 0) {
            return 0;
        }
        int extras = totalAspectPoints / POINTS_PER_EXTRA_CRYSTAL;
        return 1 + random.nextInt(extras + 1);
    }
}
