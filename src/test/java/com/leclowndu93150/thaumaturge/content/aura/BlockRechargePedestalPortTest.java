package com.leclowndu93150.thaumaturge.content.aura;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class BlockRechargePedestalPortTest {
    private static void returnToPlayer(Player player, ItemStack stack) throws Exception {
        Method method = BlockRechargePedestal.class.getDeclaredMethod("returnToPlayer", Player.class, ItemStack.class);
        method.setAccessible(true);
        method.invoke(null, player, stack);
    }

    @Test
    void pedestalReturnsAnyInventoryRemainderToTheWorld() throws Exception {
        Player player = mock(Player.class);
        Inventory inventory = mock(Inventory.class);
        ItemStack stack = new ItemStack(Items.STONE, 4);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.add(stack)).thenAnswer(invocation -> {
            stack.setCount(1);
            return true;
        });

        returnToPlayer(player, stack);

        verify(player).drop(same(stack), eq(false));
    }

    @Test
    void pedestalDoesNotDropAStackFullyAcceptedByInventory() throws Exception {
        Player player = mock(Player.class);
        Inventory inventory = mock(Inventory.class);
        ItemStack stack = new ItemStack(Items.STONE, 4);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.add(stack)).thenAnswer(invocation -> {
            stack.setCount(0);
            return true;
        });

        returnToPlayer(player, stack);

        verify(player, never()).drop(same(stack), eq(false));
    }
}
