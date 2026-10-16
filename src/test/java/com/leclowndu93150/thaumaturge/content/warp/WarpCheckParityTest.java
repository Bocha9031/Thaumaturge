package com.leclowndu93150.thaumaturge.content.warp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;

class WarpCheckParityTest {
    @Test
    void idleCheckDoesNotConsumeRandomnessAndStillSynchronizesWarp() {
        Fixture fixture = fixture(0, 0, 0, 0);

        try (MockedStatic<WarpManager> warpManager = warpManager(fixture, 0)) {
            WarpEvents.checkWarpEvent(fixture.player());

            verifyNoInteractions(fixture.random());
            verify(fixture.player()).syncData(TTAttachments.WARP);
        }
    }

    @Test
    void chanceImmediatelyBelowOnePercentStepRunsCheckAndSyncsAfterCounter() {
        Fixture fixture = fixture(0, 1, 0, 1);
        when(fixture.random().nextDouble()).thenReturn(Math.nextDown(0.02));
        when(fixture.random().nextInt(7)).thenReturn(0);

        try (MockedStatic<WarpManager> warpManager = warpManager(fixture, 9);
                MockedStatic<PlayerResearchAccess> researchAccess = researchAccess(fixture);
                MockedStatic<PacketDistributor> packets = mockStatic(PacketDistributor.class)) {
            WarpEvents.checkWarpEvent(fixture.player());

            InOrder order = inOrder(fixture.random(), fixture.data(), fixture.player());
            order.verify(fixture.random()).nextDouble();
            order.verify(fixture.random()).nextInt(7);
            order.verify(fixture.data()).setCounter(-4);
            order.verify(fixture.player()).syncData(TTAttachments.WARP);
            warpManager.verify(() -> WarpManager.sendActionBar(fixture.player(), "warp.thaumaturge.text.11"));
        }
    }

    @Test
    void chanceAtOnePercentStepDoesNotRunStrengthRollButStillSynchronizes() {
        Fixture fixture = fixture(0, 1, 0, 1);
        when(fixture.random().nextDouble()).thenReturn(0.02);

        try (MockedStatic<WarpManager> warpManager = warpManager(fixture, 9)) {
            WarpEvents.checkWarpEvent(fixture.player());

            verify(fixture.random()).nextDouble();
            verify(fixture.random(), never()).nextInt(anyInt());
            verify(fixture.data(), never()).setCounter(anyInt());
            verify(fixture.player()).syncData(TTAttachments.WARP);
        }
    }

    @Test
    void largeCounterUsesCappedWideEffectiveWarpAndPreservesCounterFormula() {
        Fixture fixture = fixture(0, 1, 0, Integer.MAX_VALUE);
        when(fixture.random().nextDouble()).thenReturn(0.5);
        when(fixture.random().nextInt(100)).thenReturn(0);
        int expectedCounter = (int)
                Math.min(Integer.MAX_VALUE - 5.0, Integer.MAX_VALUE - 2.0 * Math.sqrt(Integer.MAX_VALUE) + 2.0 * 9);

        try (MockedStatic<WarpManager> warpManager = warpManager(fixture, 9);
                MockedStatic<PlayerResearchAccess> researchAccess = researchAccess(fixture);
                MockedStatic<PacketDistributor> packets = mockStatic(PacketDistributor.class)) {
            WarpEvents.checkWarpEvent(fixture.player());

            InOrder order = inOrder(fixture.random(), fixture.data(), fixture.player());
            order.verify(fixture.random()).nextDouble();
            order.verify(fixture.random()).nextInt(100);
            order.verify(fixture.data()).setCounter(expectedCounter);
            order.verify(fixture.player()).syncData(TTAttachments.WARP);
        }
    }

    @Test
    void largeGearUsesWideRawArithmeticAndKeepsTheEffectiveWarpCap() {
        Fixture fixture = fixture(0, 1, 0, 1);
        when(fixture.random().nextDouble()).thenReturn(0.0);
        when(fixture.random().nextInt(100)).thenReturn(0);

        try (MockedStatic<WarpManager> warpManager = warpManager(fixture, 1_500_000_000);
                MockedStatic<PlayerResearchAccess> researchAccess = researchAccess(fixture);
                MockedStatic<GuardianSpawner> guardianSpawner = mockStatic(GuardianSpawner.class);
                MockedStatic<PacketDistributor> packets = mockStatic(PacketDistributor.class)) {
            WarpEvents.checkWarpEvent(fixture.player());

            InOrder order = inOrder(fixture.random(), fixture.data(), fixture.player());
            order.verify(fixture.random()).nextDouble();
            order.verify(fixture.random()).nextInt(100);
            order.verify(fixture.data()).setCounter(-4);
            order.verify(fixture.player()).syncData(TTAttachments.WARP);
            guardianSpawner.verify(() -> GuardianSpawner.spawn(fixture.player(), 6));
        }
    }

    private static Fixture fixture(int temporary, int normal, int permanent, int counter) {
        ServerPlayer player = mock(ServerPlayer.class);
        WarpData data = mock(WarpData.class);
        RandomSource random = mock(RandomSource.class);
        when(data.get(WarpType.TEMPORARY)).thenReturn(temporary);
        when(data.get(WarpType.NORMAL)).thenReturn(normal);
        when(data.get(WarpType.PERMANENT)).thenReturn(permanent);
        when(data.getCounter()).thenReturn(counter);
        when(player.getRandom()).thenReturn(random);
        when(player.getItemBySlot(EquipmentSlot.HEAD)).thenReturn(ItemStack.EMPTY);
        return new Fixture(player, data, random);
    }

    private static MockedStatic<WarpManager> warpManager(Fixture fixture, int gear) {
        MockedStatic<WarpManager> warpManager = mockStatic(WarpManager.class);
        warpManager.when(() -> WarpManager.data(fixture.player())).thenReturn(fixture.data());
        warpManager.when(() -> WarpManager.getWarpFromGear(fixture.player())).thenReturn(gear);
        return warpManager;
    }

    private static MockedStatic<PlayerResearchAccess> researchAccess(Fixture fixture) {
        PlayerResearch research = mock(PlayerResearch.class);
        when(research.knows(any(ResourceLocation.class))).thenReturn(true);
        MockedStatic<PlayerResearchAccess> access = mockStatic(PlayerResearchAccess.class);
        access.when(() -> PlayerResearchAccess.of(fixture.player())).thenReturn(research);
        return access;
    }

    private record Fixture(ServerPlayer player, WarpData data, RandomSource random) {}
}
