package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

final class PechLoadout {
    private static final int TOTAL_WEIGHT = 100;
    private static final List<Option> OPTIONS = List.of(
            new Option(10, TTItems.PECH_WAND::get),
            new Option(5, () -> Items.STONE_SWORD),
            new Option(25, () -> Items.BOW),
            new Option(5, () -> Items.STONE_AXE),
            new Option(5, () -> Items.IRON_SWORD),
            new Option(5, () -> Items.IRON_AXE),
            new Option(5, () -> Items.FISHING_ROD),
            new Option(5, () -> Items.STONE_PICKAXE),
            new Option(5, () -> Items.IRON_PICKAXE),
            new Option(30, () -> Items.AIR));

    private PechLoadout() {}

    static Item roll(RandomSource random) {
        int remaining = random.nextInt(TOTAL_WEIGHT);
        for (Option option : OPTIONS) {
            remaining -= option.weight();
            if (remaining < 0) {
                return option.item().get();
            }
        }
        return Items.AIR;
    }

    private record Option(int weight, Supplier<? extends Item> item) {}
}
