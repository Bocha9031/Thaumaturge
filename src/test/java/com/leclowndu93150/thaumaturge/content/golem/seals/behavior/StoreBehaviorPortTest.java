package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class StoreBehaviorPortTest {
    @Test
    void onlyItemsActuallyAcceptedByTheDestinationCountAsDelivered() {
        ItemStack rejected = new ItemStack(Items.STONE, 3);
        assertEquals(0, StoreBehavior.placedCount(3, rejected));
        assertEquals(5, StoreBehavior.placedCount(8, rejected));
        assertEquals(8, StoreBehavior.placedCount(8, ItemStack.EMPTY));
    }
}
