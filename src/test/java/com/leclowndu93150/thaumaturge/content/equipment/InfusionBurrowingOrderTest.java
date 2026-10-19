package com.leclowndu93150.thaumaturge.content.equipment;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class InfusionBurrowingOrderTest {
    @Test
    void burrowingHarvestsBeforeDamagingTheTool() {
        ServerLevel level = mock(ServerLevel.class);
        Player player = mock(Player.class);
        ItemStack tool = mock(ItemStack.class);
        BlockState state = mock(BlockState.class);
        BlockPos pos = new BlockPos(2, 64, 3);
        BreakEvent event = mock(BreakEvent.class);
        when(event.getLevel()).thenReturn(level);
        when(event.getPlayer()).thenReturn(player);
        when(event.getState()).thenReturn(state);
        when(event.getPos()).thenReturn(pos);
        when(player.getMainHandItem()).thenReturn(tool);
        when(tool.isCorrectToolForDrops(state)).thenReturn(true);

        try (MockedStatic<InfusionEnchantmentHelper> enchantments = mockStatic(InfusionEnchantmentHelper.class);
                MockedStatic<EnchantMining> mining = mockStatic(EnchantMining.class)) {
            enchantments
                    .when(() -> InfusionEnchantmentHelper.has(tool, InfusionEnchantment.BURROWING))
                    .thenReturn(true);
            mining.when(() -> EnchantMining.isLog(level, pos)).thenReturn(true);
            mining.when(() -> EnchantMining.isOre(level, pos)).thenReturn(false);
            mining.when(() -> EnchantMining.breakFurthest(level, pos, state, player))
                    .thenAnswer(invocation -> {
                        verify(tool, never()).hurtAndBreak(anyInt(), eq(player), eq(EquipmentSlot.MAINHAND));
                        return true;
                    });

            InfusionEnchantmentEvents.onBreakBlock(event);
        }

        verify(event).setCanceled(true);
        verify(tool).hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }
}
