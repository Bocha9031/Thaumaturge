package com.leclowndu93150.thaumaturge.content.taint.spread;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.Test;

class FibreGrowthCompatibilityTest {
    @Test
    void growthDoesNotSampleRandomnessOrReadBlocksWhenOriginChunkIsNotLoaded() {
        ServerLevel level = mock(ServerLevel.class);
        BlockPos origin = new BlockPos(32, 72, -48);
        when(level.hasChunkAt(origin)).thenReturn(false);

        TaintHelper.attemptFibreGrowth(level, origin, true);

        verify(level).hasChunkAt(origin);
        verify(level, never()).getRandom();
        verify(level, never()).getBlockState(origin);
    }
}
