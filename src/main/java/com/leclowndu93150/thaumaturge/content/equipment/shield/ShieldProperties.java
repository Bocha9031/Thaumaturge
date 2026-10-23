package com.leclowndu93150.thaumaturge.content.equipment.shield;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BlocksAttacks;

public final class ShieldProperties {
    private static final float BLOCK_DELAY_SECONDS = 0.25F;
    private static final float DISABLE_COOLDOWN_SCALE = 1.0F;
    private static final float BLOCKING_ANGLE = 90.0F;
    private static final float FULL_REDUCTION = 1.0F;
    private static final float ITEM_DAMAGE_THRESHOLD = 3.0F;
    private static final float ITEM_DAMAGE_BASE = 1.0F;
    private static final float ITEM_DAMAGE_FACTOR = 1.0F;

    private ShieldProperties() {}

    public static Item.Properties shield(Item.Properties properties, int durability, TagKey<Item> repairItems) {
        return properties.durability(durability).repairable(repairItems).equippableUnswappable(EquipmentSlot.OFFHAND)
                .delayedComponent(DataComponents.BLOCKS_ATTACKS,
                        context -> new BlocksAttacks(BLOCK_DELAY_SECONDS, DISABLE_COOLDOWN_SCALE, List.of(new BlocksAttacks.DamageReduction(BLOCKING_ANGLE, Optional.empty(), 0.0F, FULL_REDUCTION)),
                                new BlocksAttacks.ItemDamageFunction(ITEM_DAMAGE_THRESHOLD, ITEM_DAMAGE_BASE, ITEM_DAMAGE_FACTOR), Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                                Optional.of(SoundEvents.SHIELD_BLOCK), Optional.of(SoundEvents.SHIELD_BREAK)))
                .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK);
    }
}
