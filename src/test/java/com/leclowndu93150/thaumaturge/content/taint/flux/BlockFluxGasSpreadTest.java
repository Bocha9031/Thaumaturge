package com.leclowndu93150.thaumaturge.content.taint.flux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class BlockFluxGasSpreadTest {
    @Test
    void sidewaysSpreadSkipsUnloadedNeighborsBeforeReadingTheirStates() throws Exception {
        BlockFluxGas gas = (BlockFluxGas) BlockFluxGas.gasBlockState(1).getBlock();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        when(level.hasChunkAt(any(BlockPos.class))).thenReturn(false);

        invokeSpread(gas, level, BlockPos.ZERO, 4, random);

        verify(level).hasChunkAt(BlockPos.ZERO.north());
        verify(level).hasChunkAt(BlockPos.ZERO.south());
        verify(level).hasChunkAt(BlockPos.ZERO.west());
        verify(level).hasChunkAt(BlockPos.ZERO.east());
        verify(level, never()).getBlockState(any(BlockPos.class));
        verify(level, never()).setBlock(any(BlockPos.class), any(), anyInt());
        verify(random, never()).nextInt(anyInt());
    }

    @Test
    void loadedPositiveNeighborStillParticipatesInTheExistingSpread() throws Exception {
        BlockFluxGas gas = (BlockFluxGas) BlockFluxGas.gasBlockState(1).getBlock();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos target = BlockPos.ZERO.north();
        when(level.hasChunkAt(target)).thenReturn(true);
        when(level.getBlockState(target)).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(Blocks.AIR.defaultBlockState());

        invokeSpread(gas, level, BlockPos.ZERO, 4, random);

        verify(level, times(2)).getBlockState(target);
        verify(level, never()).getBlockState(BlockPos.ZERO.south());
        verify(level, never()).getBlockState(BlockPos.ZERO.west());
        verify(level, never()).getBlockState(BlockPos.ZERO.east());
        verify(level).setBlock(BlockPos.ZERO, BlockFluxGas.gasBlockState(2), Block.UPDATE_ALL);
        verify(level).setBlock(target, BlockFluxGas.gasBlockState(2), Block.UPDATE_ALL);
    }

    private static void invokeSpread(BlockFluxGas gas, ServerLevel level, BlockPos pos, int amount, RandomSource random)
            throws Exception {
        Method spread = BlockFluxGas.class.getDeclaredMethod(
                "spreadSideways", ServerLevel.class, BlockPos.class, int.class, RandomSource.class);
        spread.setAccessible(true);
        spread.invoke(gas, level, pos, amount, random);
    }
}
