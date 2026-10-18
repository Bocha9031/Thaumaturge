package com.leclowndu93150.thaumaturge.content.alchemy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class LiquidDeathEventsTest {
    @Test
    void crystalCountScalesWithTotalAspectPointsAndAlwaysHasOneBaseDrop() {
        assertEquals(0, LiquidDeathEvents.rollCrystalCount(0, RandomSource.create(0L)));
        assertEquals(1, LiquidDeathEvents.rollCrystalCount(9, RandomSource.create(0L)));

        int count = LiquidDeathEvents.rollCrystalCount(100, RandomSource.create(0L));
        assertTrue(count >= 1 && count <= 11);
    }

    @Test
    void fullDepthLiquidDeathSlowsAllMovementAxesOnce() {
        BlockPos pos = new BlockPos(4, 8, 12);
        Level level = mock(Level.class);
        Entity entity = mock(Entity.class);
        FluidState fluid = mock(FluidState.class);
        when(level.getFluidState(pos)).thenReturn(fluid);
        when(level.isClientSide()).thenReturn(true);
        when(entity.blockPosition()).thenReturn(pos);
        when(fluid.getAmount()).thenReturn(8);

        LiquidDeathFluid.applyEntityInside(level, pos, entity);

        verify(entity).makeStuckInBlock(null, new Vec3(0.38, 0.6, 0.38));
    }

    @Test
    void overlappingFluidCellsDoNotStackMovementDamping() {
        BlockPos pos = new BlockPos(4, 8, 12);
        Level level = mock(Level.class);
        Entity entity = mock(Entity.class);
        FluidState fluid = mock(FluidState.class);
        when(level.getFluidState(pos)).thenReturn(fluid);
        when(entity.blockPosition()).thenReturn(BlockPos.ZERO);
        when(fluid.getAmount()).thenReturn(8);

        LiquidDeathFluid.applyEntityInside(level, pos, entity);

        verify(entity, never()).makeStuckInBlock(null, new Vec3(0.38, 0.6, 0.38));
    }
}
