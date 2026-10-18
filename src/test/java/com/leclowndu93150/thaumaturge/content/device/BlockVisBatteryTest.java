package com.leclowndu93150.thaumaturge.content.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class BlockVisBatteryTest {
    @Test
    void partialVisDrainIsRefundedWithoutCharging() {
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockVisBattery battery = TTBlocks.VIS_BATTERY.get();
        BlockState state = battery.defaultBlockState();

        when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(false);

        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
            aura.when(() -> AuraHelper.getVis(level, BlockPos.ZERO)).thenReturn(20.0F);
            aura.when(() -> AuraHelper.getAuraBase(level, BlockPos.ZERO)).thenReturn(10);
            aura.when(() -> AuraHelper.drainVis(level, BlockPos.ZERO, 1.0F, false))
                    .thenReturn(0.4F);

            battery.tick(state, level, BlockPos.ZERO, random);

            assertEquals(0, state.getValue(BlockVisBattery.CHARGE));
            aura.verify(() -> AuraHelper.addVis(level, BlockPos.ZERO, 0.4F));
            verify(level, never()).setBlockAndUpdate(BlockPos.ZERO, state.setValue(BlockVisBattery.CHARGE, 1));
            verify(level, never()).scheduleTick(eq(BlockPos.ZERO), eq(battery), anyInt());
        }
    }
}
