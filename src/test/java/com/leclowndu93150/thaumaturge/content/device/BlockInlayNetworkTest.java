package com.leclowndu93150.thaumaturge.content.device;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class BlockInlayNetworkTest {
    @Test
    void networkRefreshDoesNotReadNeighborBlocksFromUnloadedChunks() {
        Level level = mock(Level.class);
        BlockState inlay = TTBlocks.INLAY.get().defaultBlockState();
        when(level.hasChunkAt(BlockPos.ZERO)).thenReturn(true);
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(inlay);
        when(level.hasChunkAt(argThat(pos -> !BlockPos.ZERO.equals(pos)))).thenReturn(false);

        BlockInlay.updateNetwork(level, BlockPos.ZERO);

        verify(level, never()).getBlockState(argThat(pos -> !BlockPos.ZERO.equals(pos)));
    }
}
