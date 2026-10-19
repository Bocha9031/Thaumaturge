package com.leclowndu93150.thaumaturge.api.items;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class RechargeAccessTest {
    @Test
    void chargeQueriesAndMutatorsClampAndFailClosed() {
        ItemStack stack = rechargeableStack(80, 30);
        assertTrue(RechargeAccess.isRechargeable(stack));
        assertEquals(30, RechargeAccess.storedVis(stack));
        assertEquals(50, RechargeAccess.roomFor(stack));
        assertEquals(0.375F, RechargeAccess.fillFraction(stack));
        assertEquals(50, RechargeAccess.addVis(stack, 70));
        assertEquals(80, RechargeAccess.storedVis(stack));
        assertEquals(0, RechargeAccess.roomFor(stack));
        assertFalse(RechargeAccess.spendVis(stack, 81));
        assertFalse(RechargeAccess.spendVis(stack, -1));
        assertEquals(80, RechargeAccess.storedVis(stack));
        assertTrue(RechargeAccess.spendVis(stack, 25));
        assertEquals(55, RechargeAccess.storedVis(stack));
        assertEquals(0, RechargeAccess.addVis(stack, -10));

        ItemStack ordinary = new ItemStack(Items.STICK);
        assertFalse(RechargeAccess.isRechargeable(ordinary));
        assertEquals(0, RechargeAccess.storedVis(ordinary));
        assertEquals(0, RechargeAccess.roomFor(ordinary));
        assertEquals(0.0F, RechargeAccess.fillFraction(ordinary));
        assertEquals(0, RechargeAccess.addVis(ordinary, 5));
        assertFalse(RechargeAccess.spendVis(ordinary, 0));
    }

    @Test
    void auraDrawDoesNotLoseFractionalVisAndAddsOnlyWhatWasActuallyDrained() {
        Level level = mock(Level.class);
        when(level.isClientSide()).thenReturn(false);
        BlockPos pos = new BlockPos(2, 3, 4);
        ItemStack stack = rechargeableStack(80, 0);

        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
            aura.when(() -> AuraHelper.drainVis(level, pos, 5, true)).thenReturn(0.75F);
            assertEquals(0, RechargeAccess.drawFromAura(level, stack, pos, null, 5));
            assertEquals(0, RechargeAccess.storedVis(stack));
            aura.verify(() -> AuraHelper.drainVis(level, pos, 5, false), Mockito.never());
        }

        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
            aura.when(() -> AuraHelper.drainVis(level, pos, 5, true)).thenReturn(3.8F);
            aura.when(() -> AuraHelper.drainVis(level, pos, 3, false)).thenReturn(3.0F);
            assertEquals(3, RechargeAccess.drawFromAura(level, stack, pos, null, 5));
            assertEquals(3, RechargeAccess.storedVis(stack));
        }
    }

    @Test
    void auraDrawStillRespectsPreservationProtection() {
        Level level = mock(Level.class);
        when(level.isClientSide()).thenReturn(false);
        BlockPos pos = new BlockPos(2, 3, 4);
        ItemStack stack = rechargeableStack(80, 0);
        net.minecraft.world.entity.player.Player player = mock(net.minecraft.world.entity.player.Player.class);

        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
            aura.when(() -> AuraHelper.shouldPreserveAura(level, player, pos)).thenReturn(true);

            assertEquals(0, RechargeAccess.drawFromAura(level, stack, pos, player, 1));
            assertEquals(0, RechargeAccess.storedVis(stack));
            aura.verify(() -> AuraHelper.drainVis(level, pos, 1, true), Mockito.never());
        }
    }

    private static ItemStack rechargeableStack(int capacity, int stored) {
        ItemStack stack = new ItemStack(Items.STICK);
        stack.set(TTDataComponents.RECHARGEABLE.get(), new ChargeProfile(capacity, ChargeDisplay.ALWAYS));
        stack.set(TTDataComponents.CHARGE.get(), stored);
        return stack;
    }
}
