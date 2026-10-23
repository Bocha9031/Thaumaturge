package com.leclowndu93150.thaumaturge.content.equipment;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.jspecify.annotations.Nullable;

public final class VoidShieldItem extends ShieldItem {
    public VoidShieldItem(Properties properties) {
        super(GearWarp.with(properties, VoidGearItem.VOID_GEAR_WARP));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        VoidGearItem.selfRepairTick(stack, entity);
    }

}
