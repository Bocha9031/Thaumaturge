package com.leclowndu93150.thaumaturge.content.golem.logistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class MenuGolemLogisticsDisplayTest {
    @Test
    void displayContainerRetainsAggregatedCountsAboveNormalStackLimit() {
        var display = new MenuGolemLogistics.DisplayContainer();

        display.setItem(0, new ItemStack(Items.STONE, 200));

        assertEquals(200, display.getItem(0).getCount());
        assertSame(ItemStack.EMPTY, display.removeItem(0, 1));
        assertEquals(200, display.getItem(0).getCount());
    }
}
