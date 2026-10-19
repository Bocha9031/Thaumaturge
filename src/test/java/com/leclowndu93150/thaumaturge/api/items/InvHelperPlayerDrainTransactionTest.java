package com.leclowndu93150.thaumaturge.api.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.transaction.Transaction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvHelperPlayerDrainTransactionTest {
    private Player player;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        player = mock(Player.class);
        inventory = new Inventory(player);
        when(player.getInventory()).thenReturn(inventory);
    }

    @Test
    void successfulDrainCanSpanMultipleSlots() {
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 2));
        inventory.setItem(1, new ItemStack(Items.IRON_INGOT, 3));

        assertTrue(InvHelper.takeFromPlayer(player, new ItemStack(Items.IRON_INGOT, 4), false));

        assertTrue(inventory.getItem(0).isEmpty());
        assertEquals(1, inventory.getItem(1).getCount());
    }

    @Test
    void failedCheckedDrainPreservesInventory() {
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 2));
        ItemStack original = inventory.getItem(0);
        ItemStack wanted = new ItemStack(Items.IRON_INGOT, 4);

        assertFalse(InvHelper.takeFromPlayer(player, wanted, false));
        assertSame(original, inventory.getItem(0));
        assertEquals(2, original.getCount());
        assertEquals(2, inventory.getItem(0).getCount());

        assertFalse(InvHelper.takeFromPlayer(player, wanted, false));
        assertEquals(2, inventory.getItem(0).getCount());
    }

    @Test
    void sharedJournalRollsBackEarlierAndPartialDrainsWhenOuterTransactionAborts() {
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 3));
        inventory.setItem(1, new ItemStack(Items.GOLD_INGOT, 1));
        ItemStack originalIron = inventory.getItem(0);
        ItemStack originalGold = inventory.getItem(1);
        InvHelper.PlayerInventoryJournal journal = new InvHelper.PlayerInventoryJournal(inventory);

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(3, journal.drain(new ItemStack(Items.IRON_INGOT, 3), ItemMatch.EXACT, 3, transaction));
            assertEquals(1, journal.drain(new ItemStack(Items.GOLD_INGOT, 2), ItemMatch.EXACT, 2, transaction));
        }

        assertSame(originalIron, inventory.getItem(0));
        assertSame(originalGold, inventory.getItem(1));
        assertEquals(3, originalIron.getCount());
        assertEquals(1, originalGold.getCount());
        assertEquals(3, inventory.getItem(0).getCount());
        assertEquals(1, inventory.getItem(1).getCount());
    }
}
