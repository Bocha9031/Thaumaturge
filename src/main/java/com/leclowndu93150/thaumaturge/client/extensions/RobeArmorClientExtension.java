package com.leclowndu93150.thaumaturge.client.extensions;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

final class RobeArmorClientExtension implements IClientItemExtensions {
    static final int DEFAULT_DYE_COLOR = 0xFF6A3880;

    @Override
    public int getDefaultDyeColor(ItemStack stack) {
        return DEFAULT_DYE_COLOR;
    }
}
