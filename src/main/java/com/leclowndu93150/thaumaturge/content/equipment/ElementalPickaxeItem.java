package com.leclowndu93150.thaumaturge.content.equipment;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;

public final class ElementalPickaxeItem extends PickaxeItem {
    private static final int IGNITE_SECONDS = 2;

    public ElementalPickaxeItem(Properties properties) {
        super(TTMaterials.TOOL_ELEMENTAL, properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide() && EquipmentCombat.canAffect(player, entity)) {
            entity.igniteForSeconds(IGNITE_SECONDS);
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}
