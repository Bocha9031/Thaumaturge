package com.leclowndu93150.thaumaturge.content.entity;

import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

final class PechHoard {
    private static final String HOARD_KEY = "hoard";
    private static final String LEGACY_HOARD_KEY = "Items";

    private PechHoard() {}

    static NonNullList<ItemStack> empty() {
        return NonNullList.withSize(EntityPech.LOOT_SLOTS, ItemStack.EMPTY);
    }

    static int filledSlots(List<ItemStack> loot) {
        int filled = 0;
        for (ItemStack stack : loot) {
            if (!stack.isEmpty()) {
                filled++;
            }
        }
        return filled;
    }

    static boolean canStore(List<ItemStack> loot, ItemStack stack) {
        for (ItemStack slot : loot) {
            if (slot.isEmpty()
                    || ItemStack.isSameItemSameComponents(slot, stack)
                            && slot.getCount() + stack.getCount() <= slot.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    static ItemStack store(List<ItemStack> loot, ItemStack stack) {
        ItemStack rest = stack.copy();
        for (ItemStack slot : loot) {
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, rest)) {
                int moved = Math.min(rest.getCount(), slot.getMaxStackSize() - slot.getCount());
                if (moved > 0) {
                    slot.grow(moved);
                    rest.shrink(moved);
                }
            }
        }
        if (rest.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (int index = 0; index < loot.size(); index++) {
            if (loot.get(index).isEmpty()) {
                loot.set(index, rest.copy());
                return ItemStack.EMPTY;
            }
        }
        return rest;
    }

    static void save(CompoundTag output, List<ItemStack> loot, HolderLookup.Provider registries) {
        NonNullList<ItemStack> contents = NonNullList.withSize(loot.size(), ItemStack.EMPTY);
        for (int index = 0; index < loot.size(); index++) {
            contents.set(index, loot.get(index));
        }
        CompoundTag inventory = new CompoundTag();
        ContainerHelper.saveAllItems(inventory, contents, registries);
        output.put(HOARD_KEY, inventory.getList(LEGACY_HOARD_KEY, Tag.TAG_COMPOUND));
    }

    static NonNullList<ItemStack> load(CompoundTag input, HolderLookup.Provider registries) {
        NonNullList<ItemStack> loot = empty();
        CompoundTag inventory = new CompoundTag();
        inventory.put(LEGACY_HOARD_KEY, input.getList(HOARD_KEY, Tag.TAG_COMPOUND));
        ContainerHelper.loadAllItems(inventory, loot, registries);
        if (filledSlots(loot) == 0 && input.contains(LEGACY_HOARD_KEY)) {
            ContainerHelper.loadAllItems(input, loot, registries);
        }
        return loot;
    }
}
