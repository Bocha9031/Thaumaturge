package com.leclowndu93150.thaumaturge.content.equipment.runic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;

class RunicShieldingTest {
    @Test
    void depletionWaitStartsOnlyOnTransitionAndTracksEveryObservedCharge() {
        RunicShieldState state = new RunicShieldState();
        state.nextCycle = 12L;

        ShieldDepletionTracker.observe(state, 2, 100L);
        assertEquals(2, state.lastCharge);
        assertEquals(12L, state.nextCycle);

        ShieldDepletionTracker.observe(state, 0, 105L);
        assertEquals(0, state.lastCharge);
        assertEquals(105L + ThaumaturgeCommonConfig.SHIELD_WAIT.get(), state.nextCycle);

        ShieldDepletionTracker.observe(state, 0, 120L);
        assertEquals(105L + ThaumaturgeCommonConfig.SHIELD_WAIT.get(), state.nextCycle);
    }

    @Test
    void grantRuleRequiresCapacityCooldownUnpreservedAuraAndEnoughVis() {
        ServerLevel level = mock(ServerLevel.class);
        ServerPlayer player = mock(ServerPlayer.class);
        BlockPos pos = BlockPos.ZERO;
        RunicShieldState state = new RunicShieldState();
        state.maxCharge = 3;
        state.nextCycle = 50L;

        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
            assertFalse(ShieldGrantRule.isDue(level, player, pos, state, 3, 50L, 1.0F));
            assertFalse(ShieldGrantRule.isDue(level, player, pos, state, 2, 49L, 1.0F));
            aura.verifyNoInteractions();

            aura.when(() -> AuraHelper.shouldPreserveAura(level, player, pos)).thenReturn(true);
            assertFalse(ShieldGrantRule.isDue(level, player, pos, state, 2, 50L, 1.0F));
            aura.verify(() -> AuraHelper.shouldPreserveAura(level, player, pos));
            aura.verifyNoMoreInteractions();

            aura.reset();
            aura.when(() -> AuraHelper.shouldPreserveAura(level, player, pos)).thenReturn(false);
            aura.when(() -> AuraHelper.getVis(level, pos)).thenReturn(1.0F);
            assertTrue(ShieldGrantRule.isDue(level, player, pos, state, 2, 50L, 1.0F));
        }
    }

    @Test
    void grantAddsOnePointAndSkipsVisDrainOnlyWhenCostIsFree() throws ReflectiveOperationException {
        ServerLevel level = mock(ServerLevel.class);
        ServerPlayer player = mock(ServerPlayer.class);
        BlockPos pos = BlockPos.ZERO;
        RunicShieldState freeState = new RunicShieldState();
        RunicShieldState paidState = new RunicShieldState();
        Method grant = RunicShielding.class.getDeclaredMethod(
                "grant",
                ServerLevel.class,
                ServerPlayer.class,
                RunicShieldState.class,
                BlockPos.class,
                int.class,
                long.class,
                float.class);
        grant.setAccessible(true);

        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
            grant.invoke(null, level, player, freeState, pos, 2, 100L, 0.0F);
            verify(player).setAbsorptionAmount(3.0F);
            assertEquals(3, freeState.lastCharge);
            assertEquals(100L + ThaumaturgeCommonConfig.SHIELD_RECHARGE.get(), freeState.nextCycle);
            aura.verifyNoInteractions();

            grant.invoke(null, level, player, paidState, pos, 4, 200L, 1.0F);
            verify(player).setAbsorptionAmount(5.0F);
            aura.verify(() -> AuraHelper.drainVis(level, pos, 1.0F, false));
            assertEquals(5, paidState.lastCharge);
            assertEquals(200L + ThaumaturgeCommonConfig.SHIELD_RECHARGE.get(), paidState.nextCycle);
        }
    }

    @Test
    void rescanClampsShieldByLostCapacityAndUpdatesState() {
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getItemBySlot(ArgumentMatchers.any())).thenReturn(ItemStack.EMPTY);
        when(player.getAbsorptionAmount()).thenReturn(1.5F);
        RunicShieldState state = new RunicShieldState();
        state.maxCharge = 4;

        try (MockedStatic<ModList> modList = mockStatic(ModList.class)) {
            ModList mods = mock(ModList.class);
            modList.when(ModList::get).thenReturn(mods);
            when(mods.isLoaded(TTIds.CURIOS)).thenReturn(false);

            ShieldRescan.run(player, state);
        }

        assertEquals(0, state.maxCharge);
        verify(player).setAbsorptionAmount(0.0F);
    }
}
