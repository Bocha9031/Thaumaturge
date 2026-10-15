package com.leclowndu93150.thaumaturge.content.taint.flux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import org.junit.jupiter.api.Test;

class FluxGooChunkBoundaryTest {
    @Test
    void unloadedBelowAndSidesAreCheckedBeforeAnyNeighborRead() throws Exception {
        FluxGooFluid goo = (FluxGooFluid) FluxGooRefs.sourceFluid();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos origin = new BlockPos(0, 64, 0);
        when(level.getMinBuildHeight()).thenReturn(0);
        when(level.hasChunkAt(any(BlockPos.class))).thenReturn(false);

        invokeSpread(goo, level, origin, FluxGooFluid.gooBlockState(4).getFluidState(), random);

        verify(level).hasChunkAt(origin.below());
        verify(level).hasChunkAt(origin.north());
        verify(level).hasChunkAt(origin.south());
        verify(level).hasChunkAt(origin.west());
        verify(level).hasChunkAt(origin.east());
        verify(level, never()).getBlockState(any(BlockPos.class));
        verify(level, never()).getFluidState(any(BlockPos.class));
        verify(level, never()).setBlock(any(BlockPos.class), any(), anyInt());
    }

    @Test
    void unloadedBelowDoesNotPreventSpreadIntoLoadedSide() throws Exception {
        FluxGooFluid goo = (FluxGooFluid) FluxGooRefs.sourceFluid();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos origin = new BlockPos(0, 64, 0);
        BlockPos below = origin.below();
        BlockPos north = origin.north();
        FluidState twoQuanta = FluxGooFluid.gooBlockState(2).getFluidState();
        when(level.getMinBuildHeight()).thenReturn(0);
        when(level.hasChunkAt(below)).thenReturn(false);
        when(level.hasChunkAt(north)).thenReturn(true);
        when(level.getBlockState(north)).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.getFluidState(north)).thenReturn(twoQuanta);

        invokeSpread(goo, level, origin, FluxGooFluid.gooBlockState(4).getFluidState(), random);

        verify(level, never()).getBlockState(below);
        verify(level, never()).getFluidState(below);
        verify(level, never()).getBlockState(origin.south());
        verify(level, never()).getBlockState(origin.west());
        verify(level, never()).getBlockState(origin.east());
        verify(level).setBlock(origin, FluxGooFluid.gooBlockState(2), Block.UPDATE_CLIENTS);
        verify(level).setBlock(north, FluxGooFluid.gooBlockState(2), Block.UPDATE_CLIENTS);
    }

    private static void invokeSpread(
            FluxGooFluid goo, ServerLevel level, BlockPos pos, FluidState state, RandomSource random) throws Exception {
        Method spread = FluxGooFluid.class.getDeclaredMethod(
                "spreadTick", ServerLevel.class, BlockPos.class, FluidState.class, RandomSource.class);
        spread.setAccessible(true);
        spread.invoke(goo, level, pos, state, random);
    }
}
