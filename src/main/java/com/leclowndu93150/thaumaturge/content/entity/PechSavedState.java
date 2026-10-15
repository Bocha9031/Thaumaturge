package com.leclowndu93150.thaumaturge.content.entity;

import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

final class PechSavedState {
    private static final String VARIANT_KEY = "variant";
    private static final String RAGE_KEY = "rage_ticks";
    private static final String TAMED_KEY = "is_tamed";
    private static final String LEGACY_VARIANT_KEY = "PechType";
    private static final String LEGACY_RAGE_KEY = "Anger";
    private static final String LEGACY_TAMED_KEY = "Tamed";

    private PechSavedState() {}

    static void save(
            CompoundTag output,
            HolderLookup.Provider registries,
            List<ItemStack> loot,
            boolean domesticated,
            int rageTicks,
            int variant) {
        PechHoard.save(output, loot, registries);
        output.putBoolean(TAMED_KEY, domesticated);
        output.putShort(RAGE_KEY, (short) rageTicks);
        output.putByte(VARIANT_KEY, (byte) variant);
    }

    static State load(CompoundTag input, HolderLookup.Provider registries) {
        boolean domesticated =
                input.contains(TAMED_KEY) ? input.getBoolean(TAMED_KEY) : input.getBoolean(LEGACY_TAMED_KEY);
        short storedRage = input.contains(RAGE_KEY) ? input.getShort(RAGE_KEY) : input.getShort(LEGACY_RAGE_KEY);
        int rageTicks = Math.max(0, Math.min(Short.MAX_VALUE, storedRage));
        int variant = input.contains(VARIANT_KEY) ? input.getByte(VARIANT_KEY) : input.getByte(LEGACY_VARIANT_KEY);
        NonNullList<ItemStack> loot = PechHoard.load(input, registries);
        return new State(domesticated, rageTicks, variant, loot);
    }

    record State(boolean domesticated, int rageTicks, int variant, NonNullList<ItemStack> loot) {}
}
